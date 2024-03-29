import java.io.*;
import java.util.*;

public class Main {
	static int N, M, K;// 최초 한번만
	static int[][] map;// k번 공격과정 이후 0이되는 경우만 반영해야함.
	static int[][] indexMap;
	static Set<Integer> isRelated; // k번 마다 초기화 필요 -> tower의 길이만큼
	static List<Tower> towers;
	
	static class Tower implements Comparable<Tower> {
		int index; // 타워 인덱스
		int x;
		int y;
		int power;// 힘
		int attackOrder;// 공격한 순서

		public Tower(int index, int x, int y, int power, int attackOrder) {
			this.index = index;
			this.x = x;
			this.y = y;
			this.power = power;
			this.attackOrder = attackOrder;
		}

		// 공격자 선정시 사용할 정렬 기준
		// 공격력이 낮은것 - 공격순서가 큰것(최근) - 행+열이 큰것 - 열 값이 큰것
		@Override
		public int compareTo(Tower o) {
			if (this.power != o.power) {
				return Integer.compare(this.power, o.power);
			}

			if (this.attackOrder != o.attackOrder) {
				return -1 * Integer.compare(this.attackOrder, o.attackOrder);
			}

			if (this.x + this.y != o.x + o.y) {
				return -1 * Integer.compare(this.x + this.y, o.x + o.y);
			}

			return -1 * Integer.compare(this.y, o.y);

		}

		@Override
		public String toString() {
			return "Tower [index=" + index + ", x=" + x + ", y=" + y + ", power=" + power + ", attackOrder="
					+ attackOrder + "]";
		}

	}
	static class Point {
		int x;
		int y;
		List<Point> history;
	
		public Point(int x, int y, ArrayList<Point> history) {
			this.x = x;
			this.y = y;
			this.history = history;
		}
		
		
	}
	public static void main(String[] args) throws IOException {
		init();
		int ans = solve();
		System.out.println(ans);
	}

	private static void printMap() {
		System.out.println("printMap==");
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < M; j++) {
				System.out.print(map[i][j] + " ");
			}
			System.out.println();
		}
	}

	private static int solve() {
		for (int i = 1; i <= K; i++) {
			/* 선정 */
			// 0번인덱스가 마지막 인덱스를 공격한다.
			// 공격자에게 N+M을 가산
			Collections.sort(towers);
			towers.get(0).power += N + M;
			/* 공격 */
			attack();

			/* 결과 반영 */
			afterAttack(i);

			/* towers 타워의 개수가 1개면 종료 */
			if(towers.size()==1) {
				return towers.get(0).power;
			}
		}
		
		Collections.sort(towers, (o1,o2)-> -1*(o1.power - o2.power));
		System.out.println(towers);
		return towers.get(0).power;
	}

	/**
	 * end에 데미지 반영
	 * 관계된 애들 damage/2반영
	 * 관계 없는 애들 power+1
	 */
	private static void afterAttack(int order) {
		List<Tower> nextTowers = new ArrayList<>();
		//시작 타워 
		Tower first = towers.get(0);
		first.attackOrder = order;
		nextTowers.add(first);
		
		//공격(대상) 타워, 데미지 반영
		Tower last = towers.get(towers.size()-1);
		last.power -= first.power;
		
		if(last.power > 0) {
			nextTowers.add(last);
		}else {
//			//사라진 경우에는 map[][] = 0으로
			map[last.x][last.y] = 0;
		}
		
//		System.out.println("isrelated="+ isRelated);
		//관계된 타워 관계 없는 타워들 
		for(int i = 1; i < towers.size() - 1; i++) {
			Tower tower = towers.get(i);
			//공격의 영향을 받은 경우
			if(isRelated.contains(tower.index)) {
				tower.power -= first.power/2;
			}else {
			//공격의 영향을 받지 않은 경우	
				tower.power++;
			}
			
			if(tower.power > 0) {
				nextTowers.add(tower);
			}else {
//				map[tower.x][tower.y] = 0;
				tower.power = 0;
			}
			map[tower.x][tower.y] = tower.power;

		}
		
		//복사
		towers = nextTowers;
	}

	/**
	 * 시작 지점과 목표지점을 관계되었음을 표시한다.
	 */
	private static void initIsRelated() {
		isRelated = new HashSet<>();// 한번에 한번씩 초기화해야함.

	}

	private static void attack() {
		initIsRelated();// 공격 관계자들 표기
		if (!lazerAttack()) {
			initIsRelated();// 관련된 항목을 최신화함
			boomAttack();
		}
	}

	/**
	 * 포탄 던지기 주변애들 데미지 받음.
	 */
	private static void boomAttack() {
		// 시작지점
		int sx = towers.get(0).x;
		int sy = towers.get(0).y;
		// 목표지점
		int tx = towers.get(towers.size()-1).x;
		int ty = towers.get(towers.size()-1).y;

		// 8방향에 관계되었음을 표기
		// 좌상, 상, 우상, 좌, 우, 좌하 , 하, 우하
		int[] dx = { -1, -1, -1, 0, 0, 1, 1, 1 };
		int[] dy = { -1, 0, 1, -1, 1, -1, 0, 1 };
		
		//시작지점은 영향을 받지 않는다.
		for(int i = 0; i < 8; i++) {
			int nx = tx + dx[i];
			int ny = ty + dy[i];
			
			// 구간을 벗어난 경우
			if (nx == -1)
				nx = N;
			if (nx == N)
				nx = 0;
			if (ny == -1)
				ny = M;
			if (ny == M)
				ny = 0;
			
			//시작 지점인 경우 그냥 넘어가
			if(nx == sx && ny == sy) {
				continue;
			}
			
			//나머지는 관련이 있다.
			isRelated.add(indexMap[nx][ny]);
		}
		
	}

	/**
	 * 레이저 공격
	 * 
	 * @return 공격을 성공한 경우 true
	 */
	private static boolean lazerAttack() {
		// 시작지점
		int sx = towers.get(0).x;
		int sy = towers.get(0).y;
		// 목표지점
		int tx = towers.get(towers.size()-1).x;
		int ty = towers.get(towers.size()-1).y;

		boolean[][] visited = new boolean[N][M];
		// [0] = x, [1]=y
		Queue<Point> q = new LinkedList<>();
		q.add(new Point(sx, sy, new ArrayList<Point>()));
		visited[sx][sy] = true;

		// 우 하 좌 상
		int[] dx = { 0, 1, 0, -1 };
		int[] dy = { 1, 0, -1, 0 };
		while (!q.isEmpty()) {
			Point now = q.poll();

			for (int i = 0; i < 4; i++) {
				int nx = now.x + dx[i];
				int ny = now.y + dy[i];

				// 구간을 벗어난 경우
				if (nx == -1)
					nx = N - 1;
				if (nx == N)
					nx = 0;
				if (ny == -1)
					ny = M - 1;
				if (ny == M)
					ny = 0;

				// 다음칸이 0이거나 방문한 경우 종료
				if (map[nx][ny] == 0 || visited[nx][ny])
					continue;

				// 목표지점에 도달할 수 있다.
				if (nx == tx && ny == ty) {
					//관계자 표기
					for(Point point : now.history) {
						isRelated.add(indexMap[point.x][point.y]);
					}
					
					return true;
				}

				/* 관련되었다. */
				// 큐에 넣음
				ArrayList<Point> nh = new ArrayList<>();
				nh.addAll(now.history);
				nh.add(new Point(nx, ny, null));
				q.add(new Point(nx, ny, nh));
				visited[nx][ny] = true;
			}
		}
		return false;
	}

	private static void init() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer(br.readLine());
		N = Integer.parseInt(st.nextToken());
		M = Integer.parseInt(st.nextToken());
		K = Integer.parseInt(st.nextToken());

		// map초기화, map의 값이 1이상인 경우 index를 부여하며 towers에 추가한다.
		int towerIndex = 0;
		map = new int[N][M];
		indexMap = new int[N][M];
		towers = new ArrayList<>();
		for (int i = 0; i < N; i++) {
			st = new StringTokenizer(br.readLine());
			for (int j = 0; j < M; j++) {
				map[i][j] = Integer.parseInt(st.nextToken());
				if (map[i][j] > 0) {
					towers.add(new Tower(towerIndex, i, j, map[i][j], 0));
					indexMap[i][j] = towerIndex++;
				}
			}
		}
		

	}
}