import java.io.*;
import java.util.*;

/*
 * ### K번동안 아래의 내용을 반복한다.
 * 	!만약 모든 참가자들이 탈출했다면, 그냥 종료한다.
 * #참가자 이동 (동시에 이동한다).
 * 1.참가자의 위치를 담은 배열을 순회한다.
 * 1-1. 출구와 가까워지는 곳으로 한칸 이동한다.
 * 		-|x1-x2| + |y1-y2|가 거리이다.
 * 		-가까워 지지 않으면 이동하지 않는다.
 * 		-움직일 수 있는 칸이 2개 이상이라면, 상 , 하를 우선적으로 움직인다.
 * 		 (상, 하, 좌, 우 순서대로 반복하다 종료)
 * 		-한칸에 여러 참가자가 있을 수도 있다.		
 * #미로의 회전
 * 1. 출구에서 한명 이상의 참가자리를 포함한 가장 작은 정사각형을 잡는다.
 * 		-크기가 같은 정사각형이 여러개라면, 우선순위 -r이작은것, c가 작은것을 기준으로한다.
 * 2. 90도 회전시킨다.
 * 		-회전하게되면 벽의 내구도는 1이 깍인다
 * 		-벽의 내구도가 0이 되면 벽은 사라진다. 
 * 
 * ---
 * int[] dists; 참가자들의 이동거리를 기록할 배열 
 * Point exit; 출구의 좌표를 기록해야할 자료형 
 * int[][] map; 맵의 내구도를 기록할 배열
 * Set<Integer>[][] personMap; 참가자들의 위치를 저장할 맵
 * Point[] points; 모든 참가자의 위치를 기록하는 배열
 * 
 *  
 * */

public class Main {
	static class Point{
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
	static int n,m,k;//미로의 크기, 참가자의 수, 라운드수
	static int[] dists; //참가자들의 이동거리
	static Point exit; //출구 위치
	static Point[] points; //모든 참가자의 위치를 기록하는 배열
	static Set<Integer>[][] runnerMap;//참가자의 위치를 표기하는 배열
	static int[][] durability; //맵의 내구도를 기록할 배열
	static boolean[] isOuts;
	public static void main(String[] args) throws IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer(br.readLine());
		
		n = Integer.parseInt(st.nextToken());//미로 크기
		m = Integer.parseInt(st.nextToken());//참가자 수
		k = Integer.parseInt(st.nextToken());//라운드 수
		
		dists = new int[m + 1];
		points = new Point[m + 1];
		isOuts = new boolean[m + 1];
		runnerMap = new Set[n][n];
		durability = new int[n][n];
		
		for(int i = 0; i < n; i++){
			for(int j = 0; j < n; j++) {
				runnerMap[i][j] = new HashSet<>();
			}
		}
		
		//내구도
		for(int i = 0; i < n; i++){
			st = new StringTokenizer(br.readLine());
			for(int j = 0; j < n; j++) {
				durability[i][j] = Integer.parseInt(st.nextToken());
			}
		}
		
		
		//참가자 위치 초기화
		for(int i = 1; i <= m; i++) {
			st = new StringTokenizer(br.readLine());
			int x = Integer.parseInt(st.nextToken()) - 1;
			int y = Integer.parseInt(st.nextToken()) - 1;
			points[i] = new Point(x,y);
			runnerMap[x][y].add(i);
		}
		//초기 출구 위치 초기화
		st = new StringTokenizer(br.readLine());
		exit = new Point(Integer.parseInt(st.nextToken())-1, Integer.parseInt(st.nextToken())-1);
		
//		System.out.println("runnerMap==");
//		printRunnerMap();
//		System.out.println("durability==");
//		printDurabiltiy();
		
		for(int i = 0; i < k; i++) {
			//게임이 끝났는지 판단한다.
			if(isEnd()) {
//				System.out.println("isEnd?");
				break;
			}
//			System.out.println("!!!!!!!!!!!!!"+(i+1)+"초, 라운드");
			//모든참가자들을 움직인다.-> 이동시키며, 이동거리를 표기해야한다.
			moveRunners();
//			System.out.println("moveRunner이후 RunnerMap");
//			printRunnerMap();
			//회전한다.
			rotateLogic();
		}
		int totalDist = 0;
		for(int i = 1; i <= m; i++) {
			totalDist += dists[i];
		}
		System.out.println(totalDist);
		System.out.println((exit.x + 1) + " "+ (exit.y + 1));
	}
	
	/*모두 탈출한 경우 종료됨!*/
	private static boolean isEnd() {
		for(int i = 1;  i <= m; i++) {
			if(!isOuts[i]) {
				return false;
			}
		}
		return true;
	}

	private static void rotateLogic() {
		/*
		 * #미로의 회전
		 * 1. 출구에서 한명 이상의 참가자리를 포함한 가장 작은 정사각형을 잡는다.
		 * 		-크기가 같은 정사각형이 여러개라면, 우선순위 -r이작은것, c가 작은것을 기준으로한다.
		 * 2. 90도 회전시킨다.
		 * 		-회전하게되면 벽의 내구도는 1이 깍인다
		 * 		-벽의 내구도가 0이 되면 벽은 사라진다. 
		 * */
		
		//만들 수 있는 길이
		for(int len = 1; len < n; len++) {
			//시작위치
			for(int sx = 0; sx < n - len; sx++) {
				for(int sy = 0;  sy < n - len; sy++) {
					//출구가 포함되어 있고, 사람이 1명이상 포함되어 있어야한다.
					if(OnSquare(sx,sy,len) && containsRunner(sx,sy,len)) {
//						System.out.println("sx="+ sx+", sy="+sy+", len="+len+", exit="+exit);
						//90회던 시킨다.
						rotateDurability(sx, sy, len);//내구도 감소,exit 위치변경
						rotateRunnerMap(sx,sy,len);
						//runnerMap위치 변경, points위치변경
						return;
					}
				}
			}
		}
		
		
	}
	private static void rotateRunnerMap(int sx, int sy, int len) {
		/*
		 * #미로의 회전
		 * 1. 출구에서 한명 이상의 참가자리를 포함한 가장 작은 정사각형을 잡는다.
		 * 		-크기가 같은 정사각형이 여러개라면, 우선순위 -r이작은것, c가 작은것을 기준으로한다.
		 * 2. 90도 회전시킨다.
		 * 		-회전하게되면 벽의 내구도는 1이 깍인다
		 * 		-벽의 내구도가 0이 되면 벽은 사라진다. 
		 * 
		 * 
		 * */
		Set[][] before = new Set[len + 1][len + 1];
		for(int i = 0; i <= len; i++) {
			for(int j = 0; j <= len; j++) {
				before[i][j] = new HashSet<>();
			}
		}
		
		for (int i = 0; i <= len; i++) {
			for (int j = 0; j <= len; j++) {
				before[i][j].addAll(runnerMap[sx + i][sy + j]);
			}
		}

		
		
		
		//90도 회전 시킨다.
		Set[][] after = new Set[len + 1][len + 1];
		for(int i = 0; i <= len; i++) {
			for(int j = 0; j <= len; j++) {
				after[i][j] = new HashSet<>();
			}
		}

		for (int i = 0; i <= len; i++) {
			for (int j = 0; j <= len; j++) {
				after[i][j].addAll(before[len + 1 - j - 1][i]);
			}
		}

		// 원래 durability에 반영한다.
		for (int i = 0; i <= len; i++) {
			for (int j = 0; j <= len; j++) {
				runnerMap[sx + i][sy + j] = after[i][j];
				//points변경
				for(int index : runnerMap[sx+i][sy+j]) {
					points[index] = new Point(sx+i,sy+j);
				}
			}
		}
		
	}

	/*durability를 시계방향 90도 회전 시킨다.*/
	private static void rotateDurability(int sx, int sy, int len) {
		//출고
		durability[exit.x][exit.y] = -1;
		int[][] before = new int[len + 1][len + 1];
		for (int i = 0; i <= len; i++) {
			for (int j = 0; j <= len; j++) {
				before[i][j] = durability[sx + i][sy + j];				
			}
		}
		
		//90도 회전 시킨다.
		int[][] after = new int[len + 1][len + 1];
		
		for (int i = 0; i <= len; i++) {
			for (int j = 0; j <= len; j++) {
				after[i][j] = before[len + 1 - j - 1][i];				
			}
		}
		

		//원래 durability에 반영한다.
		for (int i = 0; i <= len; i++) {
			for (int j = 0; j <= len; j++) {
				durability[sx + i][sy + j] = after[i][j];
				//내구도 -1
				if(	durability[sx + i][sy + j] > 0) {
					durability[sx + i][sy + j]--;
				}
				
				//출구 표기
				if(durability[sx+i][sy+j]==-1) {
					exit.x = sx+i;
					exit.y = sy+j;
					durability[exit.x][exit.y] = 0;
				}
			}
		}
		
	}

	private static boolean containsRunner(int sx, int sy, int len) {
		for(int dx = 0; dx <= len; dx++) {
			for(int dy = 0;  dy <= len; dy++) {
				if(runnerMap[sx + dx][sy + dy].size() != 0) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean OnSquare(int sx, int sy, int len) {
		if(sx<= exit.x && sx + len >= exit.x 
				&&sy <= exit.y && sy + len >= exit.y) {
			return true;
		}	
			
			return false;
	}

	/*모든 참가자가 이동하는 로직*/
	private static void moveRunners() {
		int[] dx = {-1,1,0,0};
		int[] dy = {0,0,-1,1};
		//모든 참가자를 1번부터 이동시킨다.
		for(int i = 1; i <= m; i++) {
			//이미 탈출한 경우 제외한다.
			if(isOuts[i]) continue;
			Point currentPoint = points[i];
			int dist = calculateDistance(exit, currentPoint);
			//상하좌우를 순회하며 출구와 거리가 가까워 지는 경우 이동한다.
			loop: for(int dir = 0;  dir < 4; dir++) {
				int nx = currentPoint.x + dx[dir];
				int ny = currentPoint.y + dy[dir];
				if(isOnRange(nx,ny) && durability[nx][ny] <= 0) {
					int nextPosDist = calculateDistance(new Point(nx,ny), exit);
					//출구로 탈출한 경우
					if(nx == exit.x && ny == exit.y) {
						isOuts[i] = true;
						runnerMap[currentPoint.x][currentPoint.y].remove(i);
						dists[i]++;
						break loop; //이미 이동한 경우 다른 좌표는 확인할 필요가 없다.
					}
					
					//-이동이 가능한 경우, runnerMap의 위치를 변경한다. points를 변경한다. dists에 이동거리 추가	
					if(nextPosDist < dist) {
						runnerMap[currentPoint.x][currentPoint.y].remove(i);
						runnerMap[nx][ny].add(i);
						
						points[i] = new Point(nx,ny);
						
						dists[i]++;
						break loop; //이미 이동한 경우 다른 좌표는 확인할 필요가 없다.
					}
					
				}
			}
			
		}
		
	}

	private static boolean isOnRange(int x, int y) {
		if(x >= 0 && x < n && y >= 0 && y < n) {
			return true;
		}
		return false;
	}

	private static int calculateDistance(Point point1, Point point2) {

		return Math.abs(point1.x - point2.x) + Math.abs(point1.y - point2.y);
	}

	private static void printDurabiltiy() {
		System.out.println("durability==");
		for(int i = 0; i < n; i++) {
			for(int j = 0; j < n; j++) {
				System.out.print(durability[i][j]+"\t");
			}
			System.out.println();
		}
	}

	private static void printRunnerMap() {
		System.out.println("runnerMap==");

		for(int i = 0; i < n; i++) {
			for(int j = 0; j < n; j++) {
				System.out.print(runnerMap[i][j]+"\t");
			}
			System.out.println();
		}
	}
}