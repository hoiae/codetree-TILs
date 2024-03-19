import java.io.*;
import java.util.*;

public class Main {

	static int[][] map;// 맵의 정보를 담음
	static List<Integer>[][] people;// 사람의 위치를 담음
	static boolean[] personIsExit;// 사람이 탈출했는 지
	static Point[] personPosition;// 위치
	static int N, M, K;
	static Point exitDoor;
	static int moveCnt;
	static int[] dx = { -1, 1, 0, 0 };
	static int[] dy = { 0, 0, -1, 1 };

	static class Point {
		int x;
		int y;

		public Point(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public String toString() {
			return "[x=" + x + " y=" + y + "]";
		}
	}

	// 따로 따로? 한번에
	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer(br.readLine());
		N = Integer.parseInt(st.nextToken());
		M = Integer.parseInt(st.nextToken());
		K = Integer.parseInt(st.nextToken());

		map = new int[N][N];
		for (int i = 0; i < N; i++) {
			st = new StringTokenizer(br.readLine());
			for (int j = 0; j < N; j++) {
				map[i][j] = Integer.parseInt(st.nextToken());
			}
		}
//		// TODO map입력 확인
//		for (int i = 0; i < N; i++) {
//			for (int j = 0; j < N; j++) {
//				System.out.print(map[i][j] + " ");
//			}
//			System.out.println();
//		}

		personIsExit = new boolean[M];
		personPosition = new Point[M];

		for (int i = 0; i < M; i++) {
			st = new StringTokenizer(br.readLine());
			int x = Integer.parseInt(st.nextToken()) - 1;
			int y = Integer.parseInt(st.nextToken()) - 1;
//			people[x][y].add(i);
			personPosition[i] = new Point(x, y);
		}

//		System.out.println(Arrays.toString(personPosition));

		// exitDoor
		st = new StringTokenizer(br.readLine());
		int x = Integer.parseInt(st.nextToken()) - 1;
		int y = Integer.parseInt(st.nextToken()) - 1;
		exitDoor = new Point(x, y);

		// 로직
		solve();

	}

	private static void solve() {
		for (int i = 0; i < K; i++) {
//			System.out.println("K = " + (i + 1));
			// 사람이동
			movePeople();
			// 회전
			rotate();
		}
		System.out.println(moveCnt);
		System.out.println((exitDoor.x+1) +" " + (exitDoor.y+1));
	}

	// 회전로직
	private static void rotate() {
		// 회전시킬 정사각형 구간을 찾는다.
		for (int len = 1; len < N; len++) {
			for (int i = 0; i < N; i++) {
				for (int j = 0; j < N; j++) {
					if (i + len >= N || j + len >= N)
						continue;
					// 구간내에 참가자와, 출구가 있는지 확인한다.
					if (checkContainsPersonAndExit(i, j, len)) {
						rotateMap(i, j, len);
						return;
					}
				}
			}
		}

	}

	/*
	 * map의 출구를 이동시킨다. map의 벽을 이동시킨다. map벽의 내구도가 감소되면 삭제한다.
	 */
	private static void rotateMap(int x, int y, int len) {
//		System.out.println("회전반경: x="+x+" y="+y+" len="+len);
//		System.out.println("exitDoor.x =" + exitDoor.x + " xitDoor.y = " + exitDoor.y);
//		System.out.println("회전전 사람 위치");
		for(int i = 0; i < M; i++) {
			if(personIsExit[i]) continue;
//			System.out.println(personPosition[i]);
		}
		// 출구를 맵에 넣는다.
		map[exitDoor.x][exitDoor.y] = -1;// 출구는 -1

		int[][] movedMap = new int[len + 1][len + 1];
		ArrayList<Integer>[][] movedPeople = new ArrayList[len + 1][len + 1];
		for (int i = 0; i <= len; i++) {
			for (int j = 0; j <= len; j++) {
				movedPeople[i][j] = new ArrayList<>();
			}
		}

		// 회전시키고 moved~에 넣음
		for (int i = 0; i <= len; i++) {
			for (int j = 0; j <= len; j++) {
				movedMap[i][j] = map[x + len - j][y + i];
				for (int index : people[x + len - j][y + i]) {
					if(personIsExit[index]) continue;
//					System.out.println((x + len - j) + " " + (y + j));
//					System.out.println("index=" + index + " x=" + (x + i) + " y=" + (y + j));
					personPosition[index] = new Point(x + i, y + j);
				}
			}
		}

		// 회전시킨moved를 원래 map에 반영함
		for (int i = 0; i <= len; i++) {
			for (int j = 0; j <= len; j++) {
				map[x + i][y + j] = movedMap[i][j];
				// 회전한 출구를 반영함.
				if (map[x + i][y + j] == -1) {
					exitDoor.x = x + i;
					exitDoor.y = y + j;
					map[x + i][y + j] = 0;
				}

				// 양수인 경우, 벽이므로 내구도를 깎다.)
				if (map[x + i][y + j] >= 1) {
					map[x + i][y + j]--;
				}
			}
		}

//		// TODO 반영된 map조회
//		System.out.println("exitDoor.x =" + exitDoor.x + " xitDoor.y = " + exitDoor.y);
//		for (int i = 0; i < N; i++) {
//			for (int j = 0; j < N; j++) {
//				System.out.print(map[i][j] + " ");
//			}
//			System.out.println();
//		}

//		// TODO 이동후 사람 위치
//		System.out.println("회전 후 사람 위치");
//		for(int i = 0; i < M; i++) {
//			if(personIsExit[i]) continue;
//			System.out.println(personPosition[i]);
//		}
//		System.out.println("============================");
	}

	private static boolean checkContainsPersonAndExit(int x, int y, int len) {
		boolean containsPerson = false;
		for (int i = 0; i < M; i++) {
			if(personIsExit[i]) continue;
			Point person = personPosition[i];
			if (person.x >= x && person.x <= x + len && person.y >= y && person.y <= y + len) {
				containsPerson = true;
				break;
			}
		}

		boolean containsExit = false;

		if (exitDoor.x >= x && exitDoor.x <= x + len && exitDoor.y >= y && exitDoor.y <= y + len) {
			containsExit = true;
		}

		return containsPerson && containsExit;
	}

	/*
	 * 모든 사람이 이동한다. 탈출하지 못한 사람만 이동한다.[0] 가까운거리로만 이동이 가능하다. 벽이 아닌 곳으로 이동이 가능하다. 한번
	 * 이동하면 정지한다. 출구라면 탈출한다.
	 * 
	 * 이동한경우 moveCnt++ 이동한 경우 personPosition, people, personIsExit항상 고려
	 */
	private static void movePeople() {
		// TODO 함수로 분리
		people = new ArrayList[N][N];
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				people[i][j] = new ArrayList<>();
			}
		}

		for (int i = 0; i < M; i++) {
			if (personIsExit[i])
				continue;
			Point nextPosition = calNextPosition(personPosition[i]);

			// 이동후좌표
			if (nextPosition.x == exitDoor.x && nextPosition.y == exitDoor.y) {
				personIsExit[i] = true;
//				System.out.println("탈출="+i);
				continue;
			}
			
//			System.out.println("추가="+i);
			personPosition[i] = nextPosition;
			people[nextPosition.x][nextPosition.y].add(i);

		}

	}

	// 이동후 좌표를 반환함
	private static Point calNextPosition(Point person) {
		// 상하좌우로 탐색
		for (int dir = 0; dir < 4; dir++) {
			int nx = person.x + dx[dir];
			int ny = person.y + dy[dir];

			// 구간을 벗어났거나, 벽인경우 이동 불가능
			if (nx < 0 || ny < 0 || nx >= N || ny >= N || map[nx][ny] != 0) {
				continue;
			}

			// 거리비교
			if (Math.abs(person.x - exitDoor.x) + Math.abs(person.y - exitDoor.y) <= Math.abs(nx - exitDoor.x)
					+ Math.abs(ny - exitDoor.y)) {
				continue;
			}
			// 이동 가능한 경우 좌표를 반환한다.
			moveCnt++;
			return new Point(nx, ny);
		}
		return person;
	}

}