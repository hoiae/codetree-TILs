import java.io.*;
import java.util.*;

/*
 * 1.사람이동;
 * #모든 groups를 순화하며 아래를 반복한다.;
 * -groups[i].get(0)이 헤드이다.;
 * -map에서 헤드를 기준으로 이동해야함 ;
 * 0) map을 통해서 head가 위치할 다음 좌표를 찾는다. => headStore에 위치를 저장해둔다.
 * 1) map의 해당 위치들을 모두 0으로 초기화한다.;
 * 2);
 * -> i = i - 1의 값을 옮겨 담는다.;
 * -> 가장 앞자리는 i = 0에는 headStore을 대입한다.;
 * 3) map에 변경된 위치를 반영한다.;
 * 
 * 2.공 던지기;
 * round = 0부터 시작한다;
 * int num = round % (N * 4)를 구한다.;
 * 1) m = num/4 //몫;
 * 2) n = num%4 //나머지;
 * m == 0좌측 m == 1하단 m==2우측 m==3 상단;
 * 에서 총을쏜다.;
 * -해당 좌표에서 공을 던진다.;
 * #충돌이 일어난다.map[][]에 0이 아닌 값이 존재한다.;
 * 충돌이 일어난 좌표와, 그룹의 인덱스를 사용한다.;
 * -점수계산;
 * 그룹의 인덱스를 통해 groups[i]에 접근한다.;
 * 접근한 인덱스에서 현재 충돌한 좌표와 동일한 값을 찾는다.[i];
 * 찾은 (인덱스 + 1)의 제곱을 scores[그룹인덱스]에 더한다.;
 * -회전
 * groups[i]를 모두 중심을 기준으로 모두 스왑한다.
 * */
public class Main {
	static int[][] map;// 현재 위치한 경우의 인덱스르 반영함.
	static int[][] rMap;// 경로를 저장한다.
	static List<Point>[] routes;// 그룹별 이동 가능한 경로를 나타낸다.
	static List<Point>[] groups;// 그룹별 헤드~테일의 좌표를 저장한다.
	static int[] scores; // 그룹별 점수를 저장함.
	static int N, M, K;// 크기, 팀의 개수, 라운드 수
	static boolean[][] visited;
	static int round;

	static class Point implements Comparable<Point> {
		int x;
		int y;
		int order;

		public Point(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public Point(int x, int y, int order) {
			this.x = x;
			this.y = y;
			this.order = order;
		}

		@Override
		public String toString() {
			return "Point [x=" + x + ", y=" + y + ", order=" + order + "]";
		}

		// 머리부터 정렬하기 위함.
		@Override
		public int compareTo(Point o) {
			return Integer.compare(this.order, o.order);
		}
	}

	public static void main(String[] args) throws IOException {
		init();
		solve();
	}

	private static void solve() {
		for (int i = 0; i < K; i++) {
			round = i;
//			System.out.println("=======================");
//			System.out.println("round== "+round);
			// 1.그룹들 이동
			move();
			// 2.공던지기
			throwBall();
//			printMap();
		}
		System.out.println(Arrays.stream(scores).sum());

	}

	private static void throwBall() {
		int num = round % (N * 4);
		int direction = num / N; //어느 방향에서 던질지
		int add = num % N; //던지는 방향에서 몇칸 이동하는지
		
//		System.out.println("direction="+direction);
//		System.out.println("add="+add);
		//왼쪽면에서 던진다.
		if(direction == 0) {
			//시작 row = 0 + add;
			//col을 0~N-1까지 증가시키면서 닿는 점을 찾는다.
			int row = 0 + add;
			for(int col = 0; col < N; col++) {
				if(map[row][col] != 0) {
					//TODO 충돌
//					System.out.println("충돌지점! row="+row+", y="+col);
					int groupIndex = map[row][col];
					//TODO 점수계산
					List<Point> group = groups[groupIndex];
					for(int j = 0; j < group.size(); j++) {
						//충돌 지점과 동일한 인덱스 + 1의 제곱을 해당 그룹에 점수로더한다.
						if(group.get(j).x == row && group.get(j).y == col) {
							scores[groupIndex] += Math.pow(j+1, 2);
							break;
						}
					}
					//TODO 회전
					for(int j = 0; j < group.size()/2; j++) {
						int left = j;
						int right = group.size() - 1 - j;
		
						Point temp = group.get(left);
						group.set(left, group.get(right));
						group.set(right, temp);				
					}
					break; //공의 전진을 멈추는 break;
				}
				
			}
		//아래면에서 던진다.to오른쪽
		}else if(direction == 1) {
			//col은 고정이다. add만큼 더한 후 이동한다.
			int col = 0 + add;
			//row을 N-1 ~ 0까지
			for(int row = N - 1; row >= 0; row--) {
				if(map[row][col] != 0) {
					//TODO 충돌
//					System.out.println("충돌지점! row="+row+", y="+col);
					int groupIndex = map[row][col];
					//TODO 점수계산
					List<Point> group = groups[groupIndex];
					for(int j = 0; j < group.size(); j++) {
						//충돌 지점과 동일한 인덱스 + 1의 제곱을 해당 그룹에 점수로더한다.
						if(group.get(j).x == row && group.get(j).y == col) {
							scores[groupIndex] += Math.pow(j+1, 2);
							break;
						}
					}
					//TODO 회전
					for(int j = 0; j < group.size()/2; j++) {
						int left = j;
						int right = group.size() - 1 - j;
		
						Point temp = group.get(left);
						group.set(left, group.get(right));
						group.set(right, temp);				
					}
					break; //공의 전진을 멈추는 break;
				}
			}
			
		//오른쪽면에서 던진다.
		}else if(direction == 2) {
			//row는 고정이다. 아래에서 위로 올라가므로, N - 1 -  add이다.
			int row= N - 1 - add;
			//col은 N - 1 부터 0 까지 이동하며 일어난다.
			for(int col = N - 1; col >= 0; col--) {
				if(map[row][col] != 0) {
					//TODO 충돌
//					System.out.println("충돌지점! row="+row+", y="+col);
					int groupIndex = map[row][col];
					//TODO 점수계산
					List<Point> group = groups[groupIndex];
					for(int j = 0; j < group.size(); j++) {
						//충돌 지점과 동일한 인덱스 + 1의 제곱을 해당 그룹에 점수로더한다.
						if(group.get(j).x == row && group.get(j).y == col) {
							scores[groupIndex] += Math.pow(j+1, 2);
							break;
						}
					}
					//TODO 회전
					for(int j = 0; j < group.size()/2; j++) {
						int left = j;
						int right = group.size() - 1 - j;
		
						Point temp = group.get(left);
						group.set(left, group.get(right));
						group.set(right, temp);				
					}
					break; //공의 전진을 멈추는 break;
				}
			}
			
		//위쪽면에서 던진다.
		}else {
			//col은 고정이다. col은 오른쪽에서 부터 시작하므로 N-1 - add한다.
			int col = N - 1 - add;
			//row는 N - 1 부터 0 까지 이동하며 일어난다.
			for(int row = N - 1; row >= 0; row--) {
				if(map[row][col] != 0) {
					//TODO 충돌
					int groupIndex = map[row][col];
					//TODO 점수계산
					List<Point> group = groups[groupIndex];
					for(int j = 0; j < group.size(); j++) {
						//충돌 지점과 동일한 인덱스 + 1의 제곱을 해당 그룹에 점수로더한다.
						if(group.get(j).x == row && group.get(j).y == col) {
							scores[groupIndex] += Math.pow(j+1, 2);
							break;
						}
					}
					//TODO 회전
					for(int j = 0; j < group.size()/2; j++) {
						int left = j;
						int right = group.size() - 1 - j;
		
						Point temp = group.get(left);
						group.set(left, group.get(right));
						group.set(right, temp);				
					}
					break; //공의 전진을 멈추는 break;
				}
			}
		}
		
		
		/*
		 * 2.공 던지기;
		 * round = 0부터 시작한다;
		 * int num = round % (N * 4)를 구한다.;
		 * 1) m = num/4 //몫;
		 * 2) n = num%4 //나머지;
		 * m == 0좌측 m == 1하단 m==2우측 m==3 상단;
		 * 에서 총을쏜다.;
		 * -해당 좌표에서 공을 던진다.;
		 * #충돌이 일어난다.map[][]에 0이 아닌 값이 존재한다.;
		 * 충돌이 일어난 좌표와, 그룹의 인덱스를 사용한다.;
		 * -점수계산;
		 * 그룹의 인덱스를 통해 groups[i]에 접근한다.;
		 * 접근한 인덱스에서 현재 충돌한 좌표와 동일한 값을 찾는다.[i];
		 * 찾은 (인덱스 + 1)의 제곱을 scores[그룹인덱스]에 더한다.;
		 * -회전
		 * groups[i]를 모두 중심을 기준으로 모두 스왑한다.
		 * */
		
	}

	private static void move() {
		for (int i = 1; i <= M; i++) {
			List<Point> group = groups[i];
			Point head = group.get(0);
			Point behindHead = group.get(1);
			Point nextHead = null;
			// routes에서 head의 다른 자리를 찾는다.
			List<Point> route = routes[i];
			
			//head가 이동할 다음 칸을 찾는다.
			int[] dx = {-1,1,0,0};
			int[] dy = {0,0,-1,1};
			for(int dir = 0; dir < 4; dir++) {
				int nx = head.x +dx[dir];
				int ny = head.y +dy[dir];
				if(!isOnRange(nx,ny)||rMap[nx][ny] != i) continue;
				if(nx != behindHead.x || ny != behindHead.y) {
					nextHead = new Point(nx,ny);
					break;
				}
			}
			
			//기존map을 0으로 만든다.
			for(int j = 0; j < group.size(); j++) {
				Point g = group.get(j);
				map[g.x][g.y] = 0; //i는 그룹의 인덱스다.
			}
			
			//좌표이동, 뒤에서 부터 한칸씩 앞으로 전진한다.
			for(int j = group.size() - 1; j > 0 ; j--) {
				Point g = group.get(j);
				g = group.get(j - 1);
				group.set(j,g);
				
			}
			//head는 상단에서 미리 구한 다헤드 값으로 저장한다.
			group.set(0, nextHead);
			//map에 반영한다.
			for(int j = 0; j < group.size(); j++) {
				Point g = group.get(j);
				map[g.x][g.y] = i; //i는 그룹의 인덱스다.
			}
		}

//		* 1.사람이동;
//		 * #모든 groups를 순화하며 아래를 반복한다.;
//		 * -groups[i].get(0)이 헤드이다.;
//		 * -map에서 헤드를 기준으로 이동해야함 ;
//		 * 0) map을 통해서 head가 위치할 다음 좌표를 찾는다. => headStore에 위치를 저장해둔다.
//		 * 1) map의 해당 위치들을 모두 0으로 초기화한다.;
//		 * 2);
//		 * -> i = i - 1의 값을 옮겨 담는다.;
//		 * -> 가장 앞자리는 i = 0에는 headStore을 대입한다.;
//		 * 3) map에 변경된 위치를 반영한다.;
	}

	private static void init() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer(br.readLine());
		N = Integer.parseInt(st.nextToken());// 맵크기
		M = Integer.parseInt(st.nextToken());// 그룹수
		K = Integer.parseInt(st.nextToken());// 진행횟수

		map = new int[N][N];
		routes = new ArrayList[M + 1];// 그룹 인덱스는 1부터 시작한다.
		groups = new ArrayList[M + 1];// 그룹구성원의 위치
		scores = new int[M + 1];// 그룹별 점수

		for (int i = 0; i <= M; i++) {
			groups[i] = new ArrayList<>();
			routes[i] = new ArrayList<>();
		}
		// map에 위치한 사람들은 그룹의 인덱스를 표기해야한다.
		// route는 그룹별 이동가능한 경로를 List로 나타내야한다.
		// groups는 리스트로 자리를 저장한다.
		// scores는 초기화만

		int[][] temp = new int[N][N];
		for (int i = 0; i < N; i++) {
			st = new StringTokenizer(br.readLine());
			for (int j = 0; j < N; j++) {
				temp[i][j] = Integer.parseInt(st.nextToken());
			}
		}

		// bfs를 통해 모든 시작지점~종료지점을 그룹별로 찾는다.
		// bfs를 통해 모든 그룹의 이동가능한 경로를 저장한다.

		// List<point> list
		// 1.1을 찾으면 해당 기점을 시작으로 bfs를 시작한다.
		// 2. 2,3이 나오면 list에 추가한다.
		// 3. 2,3,4인 경우에 route에 추가한다.
		visited = new boolean[N][N];
		int groupIndex = 1;
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				if (temp[i][j] == 1) {
					initBfs(i, j, groupIndex++, temp);
				}
			}
		}

		// map에 각 그룹의 사람을 입력한다.
		for (int i = 0; i < groups.length; i++) {
			for (Point p : groups[i]) {
				map[p.x][p.y] = i;
			}
		}

		//이동경로를 기록한다.
		rMap = new int[N][N];
		for (int i = 0; i < routes.length; i++) {
			for (Point p : routes[i]) {
				rMap[p.x][p.y] = i;
			}
		}

	}

	private static void printMap() {
		// TODO TEST
		System.out.println("group별 인덱스 맵= map");
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				System.out.print(map[i][j] + " ");
			}
			System.out.println();
		}

//		int[][] rMap = new int[N][N];
		for (int i = 0; i < routes.length; i++) {
			for (Point p : routes[i]) {
				rMap[p.x][p.y] = i;
			}
		}
		

//		System.out.println("그룹별 루트 조회");
//		for(int i = 0 ; i< N; i++) {
//			for(int j = 0; j < N; j++) {
//				System.out.print(rMap[i][j]+" ");
//			}
//			System.out.println();
//		}

	}

	private static void initBfs(int startX, int startY, int groupIndex, int[][] temp) {
		// groups초기화
		groups[groupIndex].add(new Point(startX, startY, 1));
		// routes초기화
		routes[groupIndex].add(new Point(startX, startY));

		Queue<Point> q = new LinkedList<>();
		q.add(new Point(startX, startY));
		visited[startX][startY] = true;

		int[] dx = { -1, 1, 0, 0 };
		int[] dy = { 0, 0, -1, 1 };
		while (!q.isEmpty()) {
			Point now = q.poll();

			for (int i = 0; i < 4; i++) {
				int nx = now.x + dx[i];
				int ny = now.y + dy[i];

				// 구간을 벗어나면 그만
				if (!isOnRange(nx, ny) || visited[nx][ny] || temp[nx][ny] == 0)
					continue;
				// 1234중에 하나여야함.

				// 1머리 2 중간 3 꼬리이므로 기록한다.
				if (temp[nx][ny] < 4) {
					groups[groupIndex].add(new Point(nx, ny, temp[nx][ny]));
				}
				// 1~4모두 해당 그룹의 경로이므로 표시한다.
				routes[groupIndex].add(new Point(nx, ny));

				// 방문처리 및 큐에 넣는다.
				visited[nx][ny] = true;
				q.add(new Point(nx, ny));

			}
		}

		Collections.sort(groups[groupIndex]);

		// bfs를 통해 모든 시작지점~종료지점을 그룹별로 찾는다.
		// bfs를 통해 모든 그룹의 이동가능한 경로를 저장한다.

		// List<point> list
		// 1.1을 찾으면 해당 기점을 시작으로 bfs를 시작한다.
		// 2. 1,2,3이 나오면 list에 추가한다.
		// 3. 1,2,3,4인 경우에 route에 추가한다.
	}

	private static boolean isOnRange(int x, int y) {
		if (x < 0 || y < 0 || x >= N || y >= N) {
			return false;
		}
		return true;
	}
}