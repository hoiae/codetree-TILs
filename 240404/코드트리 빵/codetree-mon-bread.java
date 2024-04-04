import java.util.*;
import java.io.*;

//1.상좌우하 로 움직인다.
//최단거리는 이동가능한 칸으로만 이동하여 칸의 수가 최소가 되는 거리.
//모두 이동한 후 편의점에 도달한 경우 map[][] = -1
//2.베이스 캠프 고르기, 1에서와 같이 최단거리에 해당하는 곳을 의미한다.? 이동방향은? 없다.
//Point클래스의 컴패어러블 사용하기. 방향의 우선순위가 존재하지 않는다.
//모두 이동한 후 베이스캠프가 있는 칸은  map[][] = -1
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
			this.index = index;
			this.point = point;
		}

		@Override
		public String toString() {
			return "People [index=" + index + ", point=" + point + "]";
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
			if (this.dist != o.dist)
				return Integer.compare(this.dist, o.dist);
			// 행이 작은 순
			if (this.x != o.x)
				return Integer.compare(this.x, o.x);
			// 열이 작은순
			return Integer.compare(this.y, o.y);
//			// 거리가 작은순
//			if (this.dist < o.dist)
//				return Integer.compare(this.dist, o.dist);
//			// 행이 작은 순
//			if (this.x < o.x)
//				return Integer.compare(this.x, o.x);
//			// 열이 작은순
//			return Integer.compare(this.y, o.y);
		}

		@Override
		public String toString() {
			return "Point [x=" + x + ", y=" + y + ", dist=" + dist + ", history=" + history + "]";
		}

	}

	public static void main(String[] args) throws IOException {
		init();
		solve();
	}

	private static void solve() {
		int index = 0;
		while (true) {

			// 1.moving
			moving();

			// 2.aftermoving
			afterMoving();

//			if (index >= M && onBoard.size() == 0) {
////				System.out.println("위");
//				System.out.println(index + 1);
//				return;
//			}

			// 3.onboarding
			int order = onBoarding(index++);
//			System.out.println(index);
//			System.out.println("order onBoard=");
//			System.out.println(onBoard);
//			printMap();
//			System.out.println("order = " + order);
			if (order != 0) {
//				System.out.println("위");
				System.out.println((order + 1));
				return;
			}
//			System.out.println("=================================");

		}
	}

	private static void afeterMovingTest() {
		System.out.println("===\t afterMoving이후 \t===");
		System.out.println("\tonBoard");
		for (People p : onBoard) {
			System.out.println(p);
		}
		System.out.println("\tmap");
		printMap();

	}

	private static void printMap() {
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				System.out.print(map[i][j] + " ");
			}
			System.out.println();
		}
	}

	private static void movieTest() {
		System.out.println("===\tmoving이후\t===");
		System.out.println("\tonBoard");
		for (People p : onBoard) {
			System.out.println(p);
		}
		System.out.println("\tmap");
		printMap();

	}

	private static int onBoarding(int order) {
		int ans = 0;
//		System.out.println("==========bases 목록===========");

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
//			System.out.println(pq);
			Point target = pq.poll();
//			System.out.println("order=" + order + " 선택된 base=" + target);
			onBoard.add(new People(order, target));
//			//TODO
//			while(!pq.isEmpty()){
//				System.out.println(pq.poll());
//			}
			
			
			map[target.x][target.y] = -1;
		} else {
			if (onBoard.size() == 0) {
				ans = order;
			}
		}

//		System.out.println("========== 끝 bases 목록 끝 ===========");

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
		int size = onBoard.size();
		for (int i = size - 1; i >= 0; i--) {
//			System.out.println("movig onBoard=");
//			System.out.println(onBoard);

			People p = onBoard.get(i);
			Point start = p.point;
			Point target = cons[p.index];
			Point nextPosition = findNextPosition(start, target);
//			System.out.println("next position =");
//			System.out.println(nextPosition);
			// TODO메서드 확인용
			if (nextPosition.x == N) {
				onBoard.remove(i);
				continue;
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
//		System.out.println("start = "+ start);
//		System.out.println("tc = " + tc);
		// 현재 위치에서 tc까지 도달하기 위한 경로를 찾음
		boolean[][] visited = new boolean[N][N];
		Queue<Point> q = new LinkedList<>();
		start.history = new ArrayList<>();
		q.add(start);
		visited[start.x][start.y] = true;

		// 상 좌 우 하
		int dx[] = { -1, 0, 0, 1 };
		int dy[] = { 0, -1, 1, 0 };

//		System.out.println("findNextPosition=====");
		while (!q.isEmpty()) {
			Point now = q.poll();
//			System.out.println(now);
			if (now.x == tc.x && now.y == tc.y) {
				// 목적지에 도착함.
				Point result = now.history.get(0);
				result.dist = now.dist;
				return result;
			}

			for (int dir = 0; dir < 4; dir++) {
				int nx = now.x + dx[dir];
				int ny = now.y + dy[dir];

				if (nx < 0 || ny < 0 || nx >= N || ny >= N || visited[nx][ny] || map[nx][ny] == -1) {
					continue;
				}

//				System.out.println("nx=" + nx +" ny= " + ny);
				// 방문처리
				visited[nx][ny] = true;
				// 큐에 넣어야함.
				List<Point> nHistory = new ArrayList<>(now.history);
				nHistory.add(new Point(nx, ny, now.dist + 1));
				q.add(new Point(nx, ny, now.dist + 1, nHistory));
			}
		}

		// 도달할 수 없는 경우도 고려해야함
		return new Point(N, N, N * N);
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
			for (int j = 0; j < N; j++) {
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