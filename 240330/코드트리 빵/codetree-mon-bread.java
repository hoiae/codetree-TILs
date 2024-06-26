import java.util.*;
import java.io.*;

public class Main {
	static int N, M;
	static int[][] map;
	static List<Point> bases;// base캠프위치 저장
	static Point[] cons;// 편의점 위치 저장,i번째 사람이 가고싶은 곳
	static List<People> onBoard;// 움직일 수 있는 사람들의 정보(인덱스,포인트)저장
	static Set<Integer> arrive; // 도착한 사람들의 인덱스 저장 ->이동이 끝난 후 처리,이동마다 초기화

	static class People {
		int index;
		Point point;

		public People(int index, Point point) {
			super();
			this.index = index;
			this.point = point;
		}

	}

	static class Point implements Comparable<Point> {
		int x;
		int y;
		int dist;
		List<Point> history;

		public Point(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public Point(int x, int y, int dist) {
			this.x = x;
			this.y = y;
			this.dist = dist;
		}

		public Point(int x, int y, int dist, List<Point> history) {
			this.x = x;
			this.y = y;
			this.dist = dist;
			this.history = history;
		}

		@Override
		public int compareTo(Point o) {
			// 거리가 작은순
			if (this.dist < o.dist)
				return Integer.compare(this.dist, o.dist);
			// 행이 작은 순
			if (this.x < o.x)
				return Integer.compare(this.x, o.x);
			// 열이 작은순
			return Integer.compare(this.y, o.y);
		}
	}

	public static void main(String[] args) throws IOException {
		init();
		solve();
	}

	private static void solve() {
		int index = 0;
		while(true) {
			// 1.moving
			moving();
			// 2.aftermoving
			afterMoving();
			// 3.onboarding
			int order = onBoarding(index++);
			if(order != 0) {
				System.out.println((order + 1));
				return;
			}
		}
	}

	private static int onBoarding(int order) {
		int ans = 0;
		if (order < M) {
			// order을 넣음
			Point con = cons[order];
			PriorityQueue<Point> pq = new PriorityQueue<>();
			for (int i = 0; i < bases.size(); i++) {
				Point base = bases.get(i);
				if (map[base.x][base.y] == -1)
					continue;
				Point info = findNextPosition(base, con);
				pq.add(new Point(base.x, base.y, info.dist));
			}
			Point target = pq.poll();
			onBoard.add(new People(order, target));
		} else {
			if (onBoard.size() == 0) {
				ans = order;
			}
		}

		return ans;
	}

	private static void afterMoving() {
		ArrayList<People> temp = new ArrayList<>();
		// onBoard를 순회,
		// onBoard.index가 arrive에 포함되어있으면, map[][] = -1 포함되어있찌 않으면 afterMoving에 넣음
		for (int i = 0; i < onBoard.size(); i++) {
			People now = onBoard.get(i);
			// 포함하고 있는 경우
			if (arrive.contains(now.index)) {
				map[now.point.x][now.point.y] = -1;
			} else {
				temp.add(now);
			}
		}

		onBoard = temp;// 도착하지 않은 정보만 저장
	}

	/**
	 * onBoarding사람들이 움직인다.
	 */
	private static void moving() {
		arrive = new HashSet<>();
		for (int i = 0; i < onBoard.size(); i++) {
			People p = onBoard.get(i);
			Point start = p.point;
			Point target = cons[p.index];
			Point nextPosition = findNextPosition(start, target);
			// TODO메서드 확인용
			if (nextPosition == null) {
				System.out.println("nulll?");
			}

			// 위치 정보 갱신
			p.point.x = nextPosition.x;
			p.point.y = nextPosition.y;
			// 목표지점에 도착한경우
			if (cons[p.index].x == nextPosition.x && cons[p.index].y == nextPosition.y) {
				arrive.add(p.index);// movig이 끝난후 총 갱신
			}
		}
	}

	private static Point findNextPosition(Point start, Point tc) {

		// 현재 위치에서 tc까지 도달하기 위한 경로를 찾음
		boolean[][] visited = new boolean[N][N];
		Queue<Point> q = new LinkedList<>();
		start.history = new ArrayList<>();
		q.add(start);
		visited[start.x][start.y] = true;

		// 상 좌 우 하
		int dx[] = { -1, 0, 0, 1 };
		int dy[] = { 0, -1, 1, 0 };

		while (!q.isEmpty()) {
			Point now = q.poll();
			if (now.x == tc.x && now.y == tc.y) {
				// 목적지에 도착함.
				return now.history.get(0);
			}

			for (int dir = 0; dir < 4; dir++) {
				int nx = now.x + dx[dir];
				int ny = now.y + dy[dir];

				if (nx < 0 || ny < 0 || nx >= N || ny >= N || visited[nx][ny] || map[nx][ny] == -1) {
					continue;
				}
				// 방문처리
				visited[nx][ny] = true;
				// 큐에 넣어야함.
				List<Point> nHistory = new ArrayList<>(now.history);
				nHistory.add(new Point(nx, ny));
				q.add(new Point(nx, ny, now.dist + 1, nHistory));
			}
		}

		return null;
	}

	private static void init() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer(br.readLine());
		N = Integer.parseInt(st.nextToken());
		M = Integer.parseInt(st.nextToken());

		cons = new Point[M];// 인덱스별 편의점 위치, 0부터 시작한다.
		bases = new ArrayList<>();
		map = new int[N][N];
		for (int i = 0; i < N; i++) {
			st = new StringTokenizer(br.readLine());
			for (int j = 0; j < M; j++) {
				map[i][j] = Integer.parseInt(st.nextToken());
				if (map[i][j] == 1) {
					bases.add(new Point(i, j));
				}
			}
		}

		// 편의점 정보
		for (int i = 0; i < M; i++) {
			st = new StringTokenizer(br.readLine());
			int x = Integer.parseInt(st.nextToken()) - 1;
			int y = Integer.parseInt(st.nextToken()) - 1;
			cons[i] = new Point(x, y);
		}

		onBoard = new ArrayList<>();
	}

}