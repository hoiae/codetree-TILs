import java.io.*;
import java.util.*;

/*
	
	루돌프 이동 -> 1 ~ P 산타 이동(격자 밖으로 나간 산타들은 제외한다.)
		거리구하는 공식은?
		루돌프 이동
		가장 가까운 산타에게 1칸 돌진.(격자 밖으로 나간 산타들은 제외한다.)
		가까운 산타가 여러명인 경우,
		거리가 짧은 것, r좌표가 큰 것, c좌표가 큰 것
		루돌프는 8방향으로 돌진할 수 있다.
	산타의 움직임
		산타는 루돌프에게 거리가 가장 가까워지는 방향으로 1칸 이동한다. ??우선순위가 어떻게 됨?
		1 ~ P까지 순서대로 움직인다.(격자밖으로 나갔거나, 기절한 산타는 제외한다.)
		게임판 밖으로 움직일 수 없다.
		움직일 공간이 없으면 움직이지 않는다.
		가까워질 수 없으면 움직이지 않는다.
		산타는 4방향(상,하,우,좌 우선순위!)으로 움직일 수 있다.
	충돌
		산타와 루돌프가 같은 칸에 있으면 충돌이다.
		루돌프의 움직임으로 인한 충돌
			산타는 C만큼의 점수를 획득한다.
			루돌프가 이동한 방향으로 C칸만큼 밀려난다.?? -> 루돌프가 이동했던 방향 그대로 밀려난다.
		산타의 움직임으로 인한 충돌
			해당 산타는 D만큼의 점수를 얻게 된다.
			산타는 자신이 이동해온 반대 방향으로 D만큼 밀려나게 된다.??-> 루돌프로 이동했던 반대 방향
			밀려난 곳이, 게임판 밖으면 => 탈락
		밀려난 곳에 다른 산타 => '상호작용'
	상호작용
		그 산타는 ?1칸 해당방향으로 밀려난다. 자리는 빼앗긴 산타가 뒤로 밀려난다. =>bfs
		밖으로 밀려나면 탈락한다.
		기절
		산타는 '충돌' 후 기절한다.
		K번째 충돌하면, K+1은 기절 상태이다. k+2부터 정상상태
		움직일 수 없다.
	*/
//int[] score 각 산타의 점수를 나타낼 배열 
//int[][] map, 산타의 위치를 나타냄. , 루돌프는 굳이?
//Point rudolf 루돌프의 위치
//List<Santa> santas 살아있는 산타들의 좌표를 저장함
//TreeSet<Integer> dead //탈락한 산타들의 인덱스를 저장함 -> 라운드마다 뒤에서 제거해야함. 
// Class Point(int x, int y)
// Class Santa(int index, Point point, int stun)
// Class Target(Santa santa, int dist)

public class Main {
	static int N, M, P, C, D;
	static int[] score;// 산타별 점수
	static int[][] map; // map
	static Point rudolf; // 루돌프 좌표
	static List<Santa> santas; // 산타들
	static Set<Integer> dead; // 죽은산타 -> 매 턴마다 갱신필요
	static int round;

	static class Point {
		int x;
		int y;

		public Point(int x, int y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public String toString() {
			return "Point [x=" + x + ", y=" + y + "]";
		}

	}

	static class Santa implements Comparable<Santa> {
		int index;
		Point point;
		int stun;

		public Santa(int index, Point point, int stun) {
			this.index = index;
			this.point = point;
			this.stun = stun;
		}

		// 산타는 순서대로 이동해야 하므로,
		@Override
		public int compareTo(Santa o) {

			return Integer.compare(this.index, o.index);
		}

		@Override
		public String toString() {
			return "Santa [index=" + index + ", point=" + point + ", stun=" + stun + "]";
		}

	}

	static class Target implements Comparable<Target> {
		Santa santa;
		int dist;

		public Target(Santa santa, int dist) {
			this.santa = santa;
			this.dist = dist;
		}

		@Override
		public int compareTo(Target o) {
			if (this.dist != o.dist) {
				return Integer.compare(this.dist, o.dist);
			}

			if (this.santa.point.x != o.santa.point.x) {
				return -1 * Integer.compare(this.santa.point.x, o.santa.point.x);
			}

			if (this.santa.point.y != o.santa.point.y) {
				return -1 * Integer.compare(this.santa.point.y, o.santa.point.y);
			}

			return 0;
		}

		@Override
		public String toString() {
			return "Target [santa=" + santa + ", dist=" + dist + "]";
		}

	}

	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer(br.readLine());
		N = Integer.parseInt(st.nextToken());
		M = Integer.parseInt(st.nextToken());
		P = Integer.parseInt(st.nextToken());
		C = Integer.parseInt(st.nextToken());
		D = Integer.parseInt(st.nextToken());

		// 루돌프 좌표
		st = new StringTokenizer(br.readLine());
		int x = Integer.parseInt(st.nextToken()) - 1;
		int y = Integer.parseInt(st.nextToken()) - 1;
		rudolf = new Point(x, y);

		map = new int[N][N];
		score = new int[N + 1]; // 인덱스는 1부터 시작한다.
		santas = new ArrayList<>();
		dead = new TreeSet<>(); // 라운드 마다 초기화해야한다.

		// 산타 위치를 입력받음
		for (int i = 0; i < P; i++) {
			st = new StringTokenizer(br.readLine());
			int index = Integer.parseInt(st.nextToken());
			int sx = Integer.parseInt(st.nextToken()) - 1;
			int sy = Integer.parseInt(st.nextToken()) - 1;
			santas.add(new Santa(index, new Point(sx, sy), 0));

			// map에 산타의 인덱스를 표기한다.
			map[sx][sy] = index;
		}

		solve();
	}

	private static void solve() {
		for (int i = 0; i < M; i++) {
			round = i;
//			System.out.println("***************" + (i + 1) + "번째 turn");
			// 1.루돌프이동
			moveRudolf();
//			System.out.println("루돌프이동후");
//			printMap();
			// 2.산타 이동
			moveSanta();

			// 3.생존자 점수 부여 -> 생존자 없으면 종료!!
			if (santas.size() == 0) {
				return;
			}

//			System.out.println("산타 이동 후");
//			printMap();
			assignScore();
//			System.out.println("score= " + Arrays.toString(score));

		}

		// TODO score출력한다. 1 ~ P까지
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i <= P; i++) {
			sb.append(score[i] + " ");
		}
		System.out.println(sb);
	}

	private static void assignScore() {
		for (Santa santa : santas) {
			score[santa.index]++;
		}

	}

	private static void printMap() {
		int[][] temp = new int[N][N];
		for (int i = 0; i < N; i++) {
			temp[i] = map[i].clone();
		}

		temp[rudolf.x][rudolf.y] = -1;

		System.out.println("====    map    ====");
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				System.out.print(temp[i][j] + " ");
			}
			System.out.println();
		}

	}

	/*
	 * 산타의 움직임 산타는 루돌프에게 거리가 가장 가까워지는 방향으로 1칸 이동한다. ??우선순위가 어떻게 됨? 1 ~ P까지 순서대로
	 * 움직인다.(격자밖으로 나갔거나, 기절한 산타는 제외한다.) 게임판 밖으로 움직일 수 없다. 움직일 공간이 없으면 움직이지 않는다. 가까워질
	 * 수 없으면 움직이지 않는다. 산타는 4방향(상,하,우,좌 우선순위!)으로 움직일 수 있다.
	 */
	private static void moveSanta() {
		dead = new HashSet<>();
		int[] dx = { -1, 0, 1, 0 };
		int[] dy = { 0, 1, 0, -1 };
		for (int i = 0; i < santas.size(); i++) {
			Santa santa = santas.get(i);
			if(santa.stun != 0 && round <= santa.stun) continue;
			///////
			int minDist = calDist(rudolf, santa.point);
			int ansDx = 0;
			int ansDy = 0;
			for (int dir = 0; dir < 4; dir++) {
				int nx = santa.point.x + dx[dir];
				int ny = santa.point.y + dy[dir];

				// 이동할 수 없는 경우
				if (nx < 0 || ny < 0 || nx >= N || ny >= N || map[nx][ny] != 0) {
					continue;
				}

				// 루돌프-산타 기존거리, 루돌프 - 이동 산타 거리 비교=> 거리가 짧아지지 않는 경우
				int dist = calDist(rudolf, new Point(nx, ny));
				if (dist >= minDist) {
					continue;
				}
//				System.out.println(i+"번째 산타 "+" dir=" + dir +" dist=" + dist);
				minDist = dist;
				ansDx = dx[dir];
				ansDy = dy[dir];

				// 이동한 경우, 이동시켜야함->map과 santas. 종료,
//				map[santa.point.x][santa.point.y] = 0;
//				map[nx][ny] = santa.index;
//				santa.point.x = nx;
//				santa.point.y = ny;
//
//				// 루돌프랑 충돌한 경우
//				if (rudolf.x == santa.point.x && rudolf.y == santa.point.y) {
//					// 충돌한 산타만 D점 부여
//					score[santa.index] += D;
//					interAction(santa, dx[dir] * -1, dy[dir] * -1, D);
//				}
//				break;

			}
			
			map[santa.point.x][santa.point.y] = 0;
			map[santa.point.x + ansDx][santa.point.y + ansDy] = santa.index;
			santa.point.x += ansDx;
			santa.point.y += ansDy;

			// 루돌프랑 충돌한 경우
			if (rudolf.x == santa.point.x && rudolf.y == santa.point.y) {
				// 충돌한 산타만 D점 부여
				score[santa.index] += D;
				santa.stun = round + 1;
				interAction(santa, ansDx * -1, ansDy * -1, D);
			}
			///////
		}

		removeSanta();
	}

	/**
	 * 루돌프가 이동한다.
	 */
	private static void moveRudolf() {
		dead = new HashSet<>();// 탈락한 산타를 기록하기 위함.

		// santas를 순회하며 가장 가까운 산타를 찾는다.
		// ->루돌프와 , 산타의 거리를 비교하여 우선순위큐에 넣는다.
		// 우선순위큐에서 하나를 뽑는다. => 대상이도리산타
		PriorityQueue<Target> pq = new PriorityQueue<>();// 대상을 찾기위한 우선순위큐
		for (Santa santa : santas) {
			int dist = calDist(rudolf, santa.point);
			pq.add(new Target(santa, dist));
		}
//		System.out.println("목적 산타 정렬을 위한 pq=" + pq);
		Target target = pq.poll();
//		System.out.println("target=" + target);

		// 8방향 중 루돌프를 향해서 이동한다.
		moveRudolfToSanta(target.santa);
		// 이동 후 튕겨저 나간 산타가 있는지 확인한다.
		removeSanta();
	}

	private static void removeSanta() {
		for (int i = santas.size() - 1; i >= 0; i--) {
			Santa santa = santas.get(i);
			if (dead.contains(santa.index)) {
//				System.out.println(santa.index +" 제거됨");
				santas.remove(i);
			}
		}

//		System.out.println(santas);
	}

	private static void moveRudolfToSanta(Santa santa) {
		// 좌상 , 상, 우상, 좌, 우, 좌하, 하, 우하
		int dx[] = { -1, -1, -1, 0, 0, 1, 1, 1 };
		int dy[] = { -1, 0, 1, -1, 1, -1, 0, 1 };

		// 8방향중 가장 가까워 지는 곳을 찾고 해당지역으로 이동한다.
		int minDist = Integer.MAX_VALUE;
		int minDx = 0;
		int minDy = 0;

		for (int i = 0; i < 8; i++) {
			int nx = rudolf.x + dx[i];
			int ny = rudolf.y + dy[i];
			if (nx < 0 || ny < 0 || nx >= N || ny >= N)
				continue;
			Point np = new Point(nx, ny);
			int dist = calDist(np, santa.point);
			if (dist < minDist) {
				minDist = dist;
				minDx = dx[i];
				minDy = dy[i];
			}
		}

//		System.out.println("이동전 루돌프=" + rudolf);
		// 루돌프를 이동시킨다.
		rudolf.x += minDx;
		rudolf.y += minDy;
//		System.out.println("이동후 루돌프=" + rudolf);

		if (rudolf.x == santa.point.x && rudolf.y == santa.point.y) {
			// 충돌한 산타만 2점 부여
			score[santa.index] += C;
			santa.stun = round + 1;
			interAction(santa, minDx, minDy, C);

		}
		// 충돌 여부를 확인한다. 루돌프의 좌표를 확인한다.
		// 스턴여부
		// 상호작용을 시전!
		// 산타의 좌표를 바꿔줘야한다.

	}

	private static void interAction(Santa startPoint, int dx, int dy, int dist) {
//		System.out.println("interAction "+"dx="+dx+" dy="+dy+" dist="+ dist);
//		System.out.println(startPoint.point);
		Queue<Santa> q = new LinkedList<>();
		map[rudolf.x][rudolf.y] = 0;
		startPoint.point.x += dx * dist;
		startPoint.point.y += dy * dist;

		if (startPoint.point.x < 0 || startPoint.point.y < 0 || startPoint.point.x >= N || startPoint.point.y >= N) {
			dead.add(startPoint.index);
//			System.out.println(dead);
			return;
		}

		q.add(startPoint);

		// 충돌하게되는 santa의 위치를 계속 뒤로 미룬다.
		while (!q.isEmpty()) {
			Santa now = q.poll();

			int nextIndex = map[now.point.x][now.point.y];

			// 이동한 좌표 반영 -> map, santas
			map[now.point.x][now.point.y] = now.index;
			for (int i = 1; i < P; i++) {
				for (int j = 0; j < santas.size(); j++) {
					if (santas.get(j).index == now.index) {
						santas.get(j).point.x = now.point.x;
						santas.get(j).point.y = now.point.y;
					}
				}
			}

			// 다른 산타가 존재하는 경우
			if (nextIndex != 0) {
				Santa nextSanta = null;
				for (int i = 0; i < santas.size(); i++) {
					if (santas.get(i).index == nextIndex) {
						nextSanta = santas.get(i);
						break;
					}
				}
				int nx = nextSanta.point.x + dx;
				int ny = nextSanta.point.y + dy;

				if (nx < 0 || ny < 0 || nx >= N || ny >= N) {
					dead.add(nextIndex);
					continue;
				}
				q.add(new Santa(nextIndex, new Point(nx, ny), nextSanta.stun));
			}
		}
	}

	// 충돌여부 확인 및 산타 좌표변경
	private static int calDist(Point p1, Point p2) {
		return (int) (Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
	}
}