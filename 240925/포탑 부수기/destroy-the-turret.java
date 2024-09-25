import java.io.*;
import java.util.*;

public class Main {

	/*
	 * 각포탑의 공격령이 0이하가 되면 부서진다. 더 이상 공격할 수 없다.
	 * 아래의 과정이 k번 반복된다.
	 * 
	 * #부서지지 않은 포탑이 1개라면 중지!
	 * 
	 * 
	 * #공격자 선정
	 * 1. 가장 약한 포탑을 공격자로 선정한다.
	 * 	- 포탑의 공격력이 0이하면 건너뛴다.
	 * 	-공격력이 가장 낮은 포탑 ->int[] powers
	 * 	-가장 최근에 공격한 포탑 ->int[] lastAttacks
	 * 	-행,열의 합이 가장 큰 포탑 -> Point[] points
	 * 	-열의 값이 가장 큰 포탑
	 * 2. 선정된 포탑에 n+m의 공격력을 더한다.
	 * 	- powers[]에 공격력을 더한다.
	 * 
	 * #공격자의 공격
	 * 1. 가장 강한 포탑(대상)을 선정한다.
	 *	- 공격력이 가장 높은 포탑
	 * 	- 공격한지 가장 오래된 포탑
	 * 	- 행,열의 합이 가장 작은 포탑
	 * 	- 열이 가장 작은 포탑
	 * 
	 * 레이저 공격을 시도하고, 불가능하면 포판 공격을 한다.
	 * 2-1. 레이저 공격
	 * - 레이저공격은 (우 하 좌 상) 4개의 방향으로 움직인다.
	 * - 부서진 포탑이 있는 위치는 지날 수 없다.(map <= 0)
	 * - 가장자리에서 막힌 방향으로 이동하면 반대편으로 나온다.
	 * - 이동의 우선 순위는 우,하,좌,상 이다.
	 * - 피해량 계산
	 * !!이동경로를 기억해놔야한다. 만약 이동경로가 존재하면 아래의 로직을 실행하고, 종료한다.(포탑공격x)
	 * 	--공격 대상은 공격자의 powers만큼 공격력이 줄어든다. => 공격력이 0 이하면 끝 0으로 표기한다.
	 * 	--레이저 공격로에 있던 포탑은 공격자의 공격력 / 2 만큼 powers가 줄어든다.
	 *
	 * 2-2. 포탑공격
	 * - 2-1.의 레이저공격을 할 수없는 경우에 실행된다.
	 * - 공격 대상은 공격자의 공격력만큼 피해를 받는다.
	 * - 공격대상의 8방향은 공격자의 공격력 / 2 만큼  powers가 줄어든다.
	 * - 가장저리에 걸린경우, 반대방향으로 8방향을 완성시킨다.
	 * - 공격자는 피해를 받지않는다.
	 * 
	 * #포탑 정비
	 * 부서지지 않고, 공격과 무관했던 포탑은 공격력이 1씩 올라간다. -> boolean[] isAttacked;
	 * (공격자 x, 데미지를 받지도 않은 것들)
	 * isAttacked가 false이면서 lastAttack이 현재round가 아닌며, power가 0보다 큰경우 증가시킨다.
	 * */
	static class Info{
		int x;
		int y;
		Set<Integer> root;

		public Info(int x, int y, Set<Integer> root) {
			this.x = x;
			this.y = y;
			this.root = root;
		}

		@Override
		public String toString() {
			return "Info [x=" + x + ", y=" + y + ", root=" + root + "]";
		}

	}
	static class Point{
		int x;
		int y;
				
		public Point(int x, int y) {
			super();
			this.x = x;
			this.y = y;
		}

		@Override
		public String toString() {
			return "Point [x=" + x + ", y=" + y + "]";
		}
		
	}
	
	static int n,m,k;//행, 열, 라운드 수\
	static int[] powers; //타워의 파워
	static int[] lastAttacks; //마지막에 공격 당한 라운드
	static Point[] points; //타워의 위치
	static int[][] map;//타워의 인덱스를 나타낸다.
	static Set<Integer> relations;//공격받은 대상
	public static void main(String[] args) throws IOException{
		BufferedReader  br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer(br.readLine());
		n = Integer.parseInt(st.nextToken());
		m = Integer.parseInt(st.nextToken());
		k = Integer.parseInt(st.nextToken());
		
		powers = new int[ (n * m) + 1];
		lastAttacks = new int[ (n * m) + 1];
		points = new Point[ (n * m) + 1];
		map = new int[n][m];
		
		int towerIndex = 0;//타워 인덱스는 1부터 시작한다.
		for(int i = 0;  i <  n; i++) {
			st = new StringTokenizer(br.readLine());
			for(int j = 0; j < m; j++) {
				int power = Integer.parseInt(st.nextToken());
				//타워의 인덱스를 표기한다.
				map[i][j] = ++towerIndex;
				//타워의 위치를 기록한다.
				points[towerIndex] = new Point(i,j);
				//타워의 파워
				powers[towerIndex] = power;
			}
		}
		
//		printPower();
		for(int i = 1; i <=k; i++) {
//			System.out.println("이번 라운드는 "+i+" 입니다.");
//			//타워가 하나만 살아있으면 종료한다.
			if(isEnd())
				break;
			
			//1.공격자 선정
			int attacker = selectAttacker();
//			System.out.println("attacker=" + attacker);
//			System.out.println("powers=" + Arrays.toString(powers));
			int target = selectTarget();
//			System.out.println("target =" + target);
			//파워에 가점 n + m부여
			powers[attacker] += n + m;
			//마지막에 점수를 올려줄때 사용할 것
			relations = new HashSet<Integer>();
			relations.add(attacker);
			relations.add(target);
			if(!laserAttack(attacker, target)) {
//				System.out.println("boomAtack!!");
				//포탄공격 은 laser공격이 실패했을때 일어난다.
				boomAttack(attacker,target);
			}
			//공격 라운드 표시
			lastAttacks[attacker] = k;
			
//			System.out.println("공격 이후");
//			printPower();
			
			
			//이번라운드에서 아무런 영향이 없는 타워의 파워을 +1해준다.
			for(int j = 1; j <= n * m; j++) {
				if(powers[j] <= 0 || relations.contains(j)) {
					continue;
				}
				powers[j]++;
			}
//			System.out.println("정비 후");
//			printPower();
		}
		
		//남아있는 포탑중 가장 강한 포탑의 공격력을 출력한다.
		System.out.println(getMaxPower());

	}
	
	private static int getMaxPower() {
		int max = -1;
		for(int i = 1; i <= n*m; i++) {
			max = Math.max(max, powers[i]);
		}
		
		return max;
	}

	/**
	 * 폭판공격
	 * @param attacker
	 * @param target
	 */
	private static void boomAttack(int attacker, int target) {
//		 * 2-2. 포탑공격
//		 * - 공격 대상은 공격자의 공격력만큼 피해를 받는다.
//		 * - 공격대상의 8방향은 공격자의 공격력 / 2 만큼  powers가 줄어든다.
//		 * - 가장저리에 걸린경우, 반대방향으로 8방향을 완성시킨다.
//		 * - 공격자는 피해를 받지않는다.
		//8방향 노가다...?
		//1.공격대상에 공격력만큼 피해를 준다.
		powers[target] -= powers[attacker];
		
		//2.주의 8방향에 대미지를 준다.
		//좌상, 상, 우상, 좌, 우, 좌하, 하, 우하
		int[] dx = { -1, -1, -1, 0, 0, 1, 1, 1 };
		int[] dy = { -1, 0, 1, -1, 1, -1, 0, 1 };
		
		for(int i = 0; i < 8; i++) {
			int nx = points[target].x + dx[i];
			int ny = points[target].y + dy[i];
			
			if(nx == n) {
				nx = 0;
			}
			if(ny == m) {
				ny = 0;
			}
			
			if(nx == -1) {
				nx = n-1;
			}
			
			if(ny == -1) {
				ny = m-1;
			}
			//주변 데미지 계산, 공격자는 데미지를 받지않는다.
			if(map[nx][ny] 
					== attacker) continue;
			powers[map[nx][ny]] -= powers[attacker]/2;
			if(powers[map[nx][ny]] <= 0) powers[map[nx][ny]] = 0;
			relations.add(map[nx][ny]);
		}

	}
	
	/**
	 * 
	 * @param attacker
	 * @param target
	 * @return
	 */
	private static boolean laserAttack(int attacker, int target) {
		int[] dx = {0,1,0,-1};
		int[] dy = {1,0,-1,0};
		
		boolean[][] visited = new boolean[n][m];
		
		Queue<Info> q = new LinkedList<>();
		visited[points[attacker].x][points[attacker].y] = true;
		q.add(new Info(
				points[attacker].x,
				points[attacker].y,
				new HashSet<>())
				);
		
		while(!q.isEmpty()) {
			Info now = q.poll();
			for(int i = 0; i < 4; i++) {
				int nx = now.x + dx[i];
				int ny = now.y + dy[i];
				
				if(nx == n) {
					nx = 0;
				}
				if(ny == m) {
					ny = 0;
				}
				
				if(nx == -1) {
					nx = n-1;
				}
				
				if(ny == -1) {
					ny = m-1;
				}
			
				//구간안에 있고, 방문한적 없고, 0보다 큰경우 이동이 가능하다.
				if(isOnRange(nx,ny) && !visited[nx][ny] && powers[map[nx][ny]]> 0){
					//target인 경우
					if(map[nx][ny] == target) {
						//현재까지 경로에 있던 것들 현재 파워/2만큼 차감
						for(int index : now.root) {
							powers[index] -= (powers[attacker]/2);
							if(powers[index] <= 0) powers[index] = 0;
						}
						//target의 파워차감
						powers[target] -= powers[attacker];
						
						//relations에 표기
						relations.addAll(now.root);
						return true;
					}
					//아닌 경우
					visited[nx][ny] = true;
					//이동경로를 추가해준다.
					Set<Integer> nextRoot = new HashSet<>(now.root);
					nextRoot.add(map[nx][ny]);
					q.add(new Info(nx,ny,nextRoot));
				}
			}
		}
		
		return false;
	}
	private static boolean isOnRange(int x, int y) {
		if(x >= 0 && x < n && y >= 0 && y< m) {
			return true;
		}
		return false;
	}
	/**
	 * 공격 대상의 인덱스를 찾아서 반환한다.
	 * @return
	 */
	private static int selectTarget() {
//		 * #공격자의 공격
//		 * 1. 가장 강한 포탑(대상)을 선정한다.
//		 *	- 공격력이 가장 높은 포탑
//		 * 	- 공격한지 가장 오래된 포탑
//		 * 	- 행,열의 합이 가장 작은 포탑
//		 * 	- 열이 가장 작은 포탑
		
		//공격력, 최근공격한 라운드, 행, 열
		PriorityQueue<int[]> pq = new PriorityQueue<>((o1,o2)->{
			if(o1[0] != o2[0]) {
				//공격력이 더 높은 순
				return -1*(o1[0] - o2[0]);
				
			}else if(o1[1] != o2[1]) {
				//최근에 공격한 라운드가 작은 순
				return -1 * (o2[1] - o1[1]);
				
			}else if(o1[2]+o1[3]  != o2[2]+o2[3]) {
				//행, 열의 합이 작은 순
				return -1 * ((o2[2]+o2[3]) - (o1[2]+o1[3]));  
			}
			//열값이 가장작은순
			return -1 * (o2[3] - o1[3]);	
		});
		
		//우선순위 판단에 필요한 정보를  pq에 넣는다. 
		//파워, 마지막에 공격한 시점, 위치의 행, 위치의 열, 타워의 index;
		for(int i = 1; i <= n * m; i++) {
			if(powers[i] <= 0) continue;
			pq.add(new int[] {powers[i], lastAttacks[i], points[i].x, points[i].y,i});
		}
		
		int target = pq.poll()[4];
		//파워에 가점 n + m부여
		
		
		return target;
	}
	/**
	 * 공격자를 선정한다.
	 * @return
	 */
	private static int selectAttacker() {
//		 * #공격자 선정
//		 * 1. 가장 약한 포탑을 공격자로 선정한다.
//		 * 	- 포탑의 공격력이 0이하면 건너뛴다.
//		 * 	-공격력이 가장 낮은 포탑 ->int[] powers
//		 * 	-가장 최근에 공격한 포탑 ->int[] lastAttacks
//		 * 	-행,열의 합이 가장 큰 포탑 -> Point[] points
//		 * 	-열의 값이 가장 큰 포탑
//		 * 2. 선정된 포탑에 n+m의 공격력을 더한다.
//		 * 	- powers[]에 공격력을 더한다.
//		 * 
		//공격력, 최근공격한 라운드, 행, 열
		PriorityQueue<int[]> pq = new PriorityQueue<>((o1,o2)->{
			if(o1[0] != o2[0]) {
				//공격력이 더 낮은순
				return o1[0] - o2[0];
			}else if(o1[1] != o2[1]) {
				//최근에 공격한 라운드가 높은 순
				return o2[1] - o1[1];
			}else if(o1[2]+o1[3]  != o2[2]+o2[3]) {
				//행, 열의 합이 큰 포탑
				return (o2[2]+o2[3]) - (o1[2]+o1[3]);  
			}
			//열값이 가장 큰 포탑
			return o2[3] - o1[3];	
		});
		
		//우선순위 판단에 필요한 정보를  pq에 넣는다. 
		//파워, 마지막에 공격한 시점, 위치의 행, 위치의 열, 타워의 index;
		for(int i = 1; i <= n * m; i++) {
			if(powers[i] <= 0) continue;
			pq.add(new int[] {powers[i], lastAttacks[i], points[i].x, points[i].y,i});
		}
		int attacker = pq.poll()[4];
		return attacker;
	}
	private static boolean isEnd() {
		int aliveCount = 0;
		for(int i = 1; i <= (n * m); i++) {
			if(powers[i] > 0) {
				aliveCount++;
			}
		}
		
		if(aliveCount == 1)
			return true;
		return false;
	}
	private static void printPower() {
		System.out.println("==\tprintPower\t==");
		for(int i = 0;  i < n; i++) {
			for(int j= 0; j < m; j++) {
				System.out.print(powers[map[i][j]]+" ");
			}
			System.out.println();
		}
		
	}

}