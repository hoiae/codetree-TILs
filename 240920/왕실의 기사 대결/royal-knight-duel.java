import java.util.*;
import java.io.*;

/*
 * #기사이동
 * 1. 명령받은 기사가 이동한다.
 * 	(생존해 있는지 확인해야한다.)
 * 2. 이동할 위치에 다른 기사가 존재하면 다른 기사도 이동한다.
 * 3. 점수계산
 * 	-"명령을 받지 않은"모든 기사가 이동할 때, 지뢰를 밟은 수를 카운팅한다.
 * 	-"모든 기사가 이동한다면, 벽에 부딪히지않고" 지뢰를 밟은 수를 목숨에 반영한다.lifes
 * 	-lifes가 0이되면 기사는 사라진다.
 * 	-> 사라지게되는 경우 map에서 지워야한다. 이때는 어떻게지우나? 좌측상단에 동일한 인덱스를 가진 bfs를 통해 다 삭제한다.
 * 
 * #필요한 자료구조
 * 1. int[][] map
 * 2. int[] lifes
 * 3. Point[] points 좌측 상단의 좌표를 저장해놓음. h랑 w는?
 * 
 * 3. int[] currentRoundDamages
 * 4. int[][] currentRoundMap
 * */

public class Main {
	static class Point{
		int x;
		int y;
		int h;
		int w;
		
		public Point(int x, int y, int h, int w) {
			this.x = x;
			this.y = y;
			this.h = h;
			this.w = w;
		}
	}
	static int[][] map;
	static int[][] obstacles;
	static int[] lifes;
	static int[] damages;
	static Point[] points;
	static int l,n,q,k;//체스판 크기, 기사의 수, 명령의 수, 기사 체력
	public static void main(String[] args) throws IOException {
		/*초기 세팅*/
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer(br.readLine());
		l = Integer.parseInt(st.nextToken()); //맵크기
		n = Integer.parseInt(st.nextToken());//기사의수
		q = Integer.parseInt(st.nextToken());//명령 수
		map = new int[l][l];
		obstacles = new int[l][l];
		lifes = new int[n + 1];
		points = new Point[n + 1];//기사들의 좌측상단 위치
		damages = new int[n + 1];
		
		//ㅣ * ㅣ크기의 지뢰정보를 담는다.
		for(int i = 0 ; i < l; i++) {
			st = new StringTokenizer(br.readLine());
			for(int j = 0 ; j < l; j++) {
				obstacles[i][j] = Integer.parseInt(st.nextToken());
			}
		}
//		printMap(obstacles);
		
		//n개의 기사정보 순서대로 입력
		for(int i = 1; i <= n; i++) {
			st = new StringTokenizer(br.readLine());
			int r = Integer.parseInt(st.nextToken()) - 1;
			int c = Integer.parseInt(st.nextToken()) - 1;
			int h = Integer.parseInt(st.nextToken());
			int w = Integer.parseInt(st.nextToken());
			int k = Integer.parseInt(st.nextToken()); //초기 체력
			
			points[i] = new Point(r,c,h,w);
			//map에 좌표채우기
			addMap(i,r,c,h,w);
			//초기 life설정
			lifes[i] = k;
		}
		
//		System.out.println("초기의 lifes="+Arrays.toString(lifes));
		
//		printMap(map);
		
		//q번의 명령
		for(int i = 0;  i < q; i++) {
			st = new StringTokenizer(br.readLine());
			int nightsIndex = Integer.parseInt(st.nextToken());
			int dir = Integer.parseInt(st.nextToken());
			moveNight(nightsIndex,dir);
//			System.out.println((i+1)+"번 명령 수행 이후 의 map========");
//			System.out.println("lifes="+Arrays.toString(lifes));
//			printMap(map);
			
//			System.out.println("");
		}
		
		int sum = 0;
		for(int i = 1; i <= n; i++) {
			if(lifes[i] > 0) {
				sum += damages[i];				
			}
		}
		System.out.println(sum);
		
	}
	private static void moveNight(int index, int dir) {
		//!!!기사가 죽으면 map, lifes모든 자료형에 표기해야한다.
		//!!!기사가 움직일때마다 points 갱신해야한다.
		//roundMap,-> 이동하면서 기록할맵 
		//roundDamages -> 이동하면서 충돌하는 지뢰의 개수
		int[][]roundMap = new int[l][l]; //임시로 위치를 표기할 맵
		int[] roundDamages = new int[n + 1]; //이동하며 받은 데미지 기록한다.
		Set<Integer> visited = new HashSet<>(); //기존에 움직인 기사인지 표기한다.
		Queue<Integer> waitingQ = new LinkedList<>();
		waitingQ.add(index);
		visited.add(index);
		//위, 오른쪽, 아래, 왼쪽
		int[] dx = {-1,0,1,0};
		int[] dy = {0,1,0,-1};
		
		while(!waitingQ.isEmpty()) {
			int nowNightsIndex = waitingQ.poll();
			Point night = points[nowNightsIndex];
			//1.살아있는 기사인가?
			if(lifes[index] <= 0) continue;
			//2.이동할 방향으로 탐색한다.
			//본인과 다른 index를 만나면, visited, waitingQ에 추가한다.
			//obstacles=2 장애물을 만나면 or 밖을 벗어나면, 이동할 수 없으므로 모든 행위를 취소한다. return;
			//obstacles=1 이면 roundDamages를 +1 해준다.
			for (int x = 0; x < night.h; x++) {
				for (int y = 0; y < night.w; y++) {
					int nx = night.x + x + dx[dir];
					int ny = night.y + y + dy[dir];
					// 장외로 나간경우
					if (!isOnRange(nx, ny)) {
						return;
					}
					// 벽을 만난 경우
					if (obstacles[nx][ny] == 2) {
						return;
					}
					// 지뢰가 있는 경우 데미지 저장.
					if (obstacles[nx][ny] == 1 && nowNightsIndex != index) {
						roundDamages[nowNightsIndex]++;
					}

					int thisPosIndex = map[nx][ny];
					// 현재 위치에 값이 다른 기사의 인덱스인 경우
					if (thisPosIndex != 0 && thisPosIndex != nowNightsIndex && !visited.contains(thisPosIndex)) {
						visited.add(thisPosIndex);
						waitingQ.add(thisPosIndex);
					}

					// roundMap에 위치를 기록한다.
					roundMap[nx][ny] = nowNightsIndex;
				}
			}

		}
		//모든 이동이 완료된 후,points 좌측 상단 좌표를 바꿔준다.
		for(int nightIndex : visited) {
			points[nightIndex].x += dx[dir]; 
			points[nightIndex].y += dy[dir]; 
		}
		//roundMap의 값을 map에 옮긴다.
		//1. visited에 있는 모든 map의 배열을 0으로 변경한다.
		for(int i = 0;  i < l; i++) {
			for(int j = 0; j < l; j++) {
				if(visited.contains(map[i][j])) {
					map[i][j] = 0;
				}
			}
		}
		//2. roundMap에 있는 값을 map으로 옮긴다.
		for(int i = 0; i < l; i++) {
			for(int j= 0; j < l; j++) {
				if(roundMap[i][j] != 0) {
					map[i][j] = roundMap[i][j];
				}
			}
		}
		//데미지를 lifes에 반영한다.-> 0보다 작아지면 map에서 제외해야함.
		for(int i = 1; i <= n; i++) {
			lifes[i] -= roundDamages[i];
			damages[i] += roundDamages[i];
			if(visited.contains(i) && lifes[i] <=0) {
				for(int j = 0; j < l; j++) {
					for(int k= 0; k < l; k++) {
						if(map[j][k] == i) {
							map[j][k] = 0;
						}
					}
				}
			}
		}
	}
	private static boolean isOnRange(int x, int y) {
		if(x >= 0 && x < l && y >= 0 && y < l) {
			return true;
		}
		return false;
	}
	private static void printMap(int[][] map) {
		for(int i = 0; i < l; i++) {
			for(int j = 0;  j < l; j++) {
				System.out.print(map[i][j]+" ");
			}
			System.out.println();
		}
	}
	private static void addMap(int index, int r, int c, int h, int w) {
		for(int i = 0;  i < h; i++) {
			for(int j = 0; j < w; j++) {
				map[r + i][c + j] = index; 
			}
		}
		
	}

}