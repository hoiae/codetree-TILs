import java.io.*;
import java.util.*;

public class Main {
	/*
	 * '정령'은 북쪽으로만 숲에 들어올 수 있다.;
	 * 
	 * '골렘'은 총5칸을 차지한다.;
	 * 4개의 바깥 칸중 어느 곳으로든 '골렘'을 탈 수 있다.;
	 * 하지만 정령이 '골렘'을 내릴때 는 정해진 출구인 1칸으로만 가능하다.;
	 * 
	 * -골렘의 탐색 과정은 아래와같다.;
	 * 1. 남쪽으로 한칸 내려간다.골렘의 바로 하단(3칸)이 비어있어야한다.;;
	 * 2. 1로 이동할 수 없다는 서쪽방향으로 회전하며 내려간다.(왼쪽 3칸이 비어있어야한다.)
	 * 		1)서쪽으로 먼저이동;
	 * 		2)출구가 반시계 방향으로 90도 회전한다.;
	 * 		3)아래로 이동;
	 * 
	 * 3. 2로 이동할 수 없다면 동쪽방향으로 회전하며 내려간다.;
	 * 	1)오른쪽으로 이동(오른쪽 3칸이 모두 비어있어야한다.);
	 * 	2)회전(출구가 시계 방향으로 90도 회전한다.);
	 * 	3)아래로 이동;
	 * 
	 * -골렘이 가장 아래로 도달하고 더이상 이동할 수 없는 경우, 정령은 골렘 내에서 ;
	 * '상하좌우' 인접한 곳으로 이동이 가능하다.;
	 * => 다른 골렘으로 이동하기 위해서 '출구'를 사용해야함에 유의한다.
	 * 이동할 수 있는 곳중 가장 아래로 이동한다;
	 * => 최종 위치를 기록해야한다.
	 * 
	 * -골렘의 몸의 일부가 숲에서 벗어난 상태라면, 모든 골렘을 치우고 다음 골렘부터 숲의 탐색을 시작한다.
	 * => 정답에 포함시키지 않는다.
	 * */
	static int[] dirs = {0,1,2,3};//북, 동, 남, 서
	static int[] dx = {-1,0,1,0};
	static int[] dy = {0,1,0,-1};
	static int r;//행 숲의 크기,
	static int c;//열 숲의 크기,
	static int k;//정령의 수
	static int cSum;//최종 이동한 후의 위치를 저장하는 함수
	static int[][] map;//숲, //1은 골렘 2는 출구로 표현한다.;
	static BufferedReader br;
	static Fairy fairy;
	static int id;
	static int[][] idMap;

	static class Fairy{
		int x;//정령의 위치, x 골렘의 중심
		int y;//정령의 위치, y
		int exitDir;//출구의 방향 0,1,2,3중 하나만 가능하다.
		public Fairy(int x, int y, int exitDir) {
			this.x = x;
			this.y = y;
			this.exitDir = exitDir;
		}
		//90도 정방향 회전
		public void rotate() {
			exitDir++;
			if(exitDir == 4) {
				exitDir = 0;
			}
		}
		//90도 반시계 회전
		public void reverseRotate() {
			exitDir--;
			if(exitDir == -1) {
				exitDir = 3;
			}
		}
		@Override
		public String toString() {
			return "Fairy [x=" + x + ", y=" + y + ", exitDir=" + exitDir + "]";
		}
		
		
	}
	public static void main(String[] args) throws IOException{
		br = new BufferedReader(new InputStreamReader(System.in));
		//init
		init();
		
		//정령 수 만큼 반복한다.
		for(int i = 0 ; i < k; i++) {
			StringTokenizer st = new StringTokenizer(br.readLine());
			int col = Integer.parseInt(st.nextToken()) - 1;
			int dir = Integer.parseInt(st.nextToken());
			fairy = new Fairy(1,col,dir);
			
			solve(fairy);
		}
		
		System.out.println(cSum);
		
		
	}
	/**
	 * 골렘을 최하단으로 이동시킨 후, 정령을 최하단으로 이동시킨 위치의 c합을 구하는 함수
	 * @param fairy
	 */
	private static void solve(Fairy start) {
		//행을 늘려도 상관 없지 않은가?
		//TODO map의 행을 + 3를 해준다.!!!!!
		//정령의 r은 1부터 시작한다.
		//숲에 포함되기 위해서 정령의 r이 4이상이어야한다.
		//이동 가능할때까지 이동시키는 함수
		moveGol(start);
		if(fairy.x >= 3) {
			//골렘의 위치를 map에 기록함
			record(fairy);
			moveFairy(fairy);
//			printMap();
		}else {
			idMap = new int[r + 3][c];
			//숲 초기화
			map = new int[r + 3][c];
		}
		//숲 내부에 존재하면, 정령을 최 하단으로 이동시키는 함수-> 컬럼의 위치를 합산해야한다.
		
	}
	private static void printMap() {
		System.out.println("map!");
		for(int i = 0;  i < map.length; i++) {
			for(int j = 0; j < c; j++) {
				System.out.print(map[i][j]+" ");
			}
			System.out.println();
		}
		
		System.out.println("idMap!");
		for(int i = 0;  i < map.length; i++) {
			for(int j = 0; j < c; j++) {
				System.out.print(idMap[i][j]+" ");
			}
			System.out.println();
		}
		
		
		System.out.println("=================================");
		System.out.println("=================================");
	}
	
	private static void record(Fairy fairy) {
		int x = fairy.x;
		int y = fairy.y;
		map[x][y] = 1;
		idMap[x][y] = ++id;
		for(int i = 0; i < 4; i++) {
			map[x + dx[i]][y + dy[i]] = 1;
			idMap[x + dx[i]][y + dy[i]] = id;
		}	
		map[x+ dx[fairy.exitDir]][y+dy[fairy.exitDir]] = 2;//출구는 2로 표기
	}
	/**
	 * 페어리를 이동이 가능한 가장 아래로 이동시킨 후 cSum에 해당 R + 4(임의로 3을 더했고, 인덱스이므로)의 값을 더한다.
	 * @param fairy
	 */
	private static void moveFairy(Fairy fairy) {
		int maxR = -1;
		//이동할 수 있는 모든 곳을 찾는다.
		boolean[][] visited = new boolean[r + 3][c];
		Queue<int[]> q = new LinkedList<>();
		
		visited[fairy.x][fairy.y] = true;
		q.add(new int[] {fairy.x, fairy.y, idMap[fairy.x][fairy.y]});
//		System.out.println("x=" + fairy.x +"y=" + fairy.y);

		maxR = fairy.x;
		while(!q.isEmpty()) {
			int[] now = q.poll();
//			System.out.println("now x="+ now[0]+", now y="+now[1]);
			maxR = Math.max(maxR, now[0]);
			for(int i = 0;  i < 4; i++) {
				int nx = now[0] + dx[i];
				int ny = now[1] + dy[i];
				if(nx < 0 || nx >= r + 3 || ny < 0 || ny >= c || visited[nx][ny] || map[nx][ny] == 0) {
					continue;
				}
				//현재 id와 다음에 도달할 곳의 id가 동일하면 그냥 이동
				if(idMap[now[0]][now[1]] == idMap[nx][ny]) {
					visited[nx][ny] = true;
					q.add(new int[] {nx, ny, idMap[nx][ny]});
				}else {
					if(map[now[0]][now[1]] == 2) {
						visited[nx][ny] = true;
						q.add(new int[] {nx, ny, idMap[nx][ny]});

					}
				}
			}
		}
		cSum += maxR - 2;
//		System.out.println("maxR="+(maxR - 2)+", cSum="+ cSum);
	}
	
	/**
	 * 골렘을 최대한 아래로 이동시키고, 해당 정보를 반환하는 함수
	 * @param fairy
	 */
	private static void moveGol(Fairy fairy) {
		//아래의 3칸이 모두 비어있는 경우 아래로 이동한다.
		if(fairy.x + 2 < r + 3 
			&& map[fairy.x + 1][fairy.y - 1] == 0 
			&& map[fairy.x + 2][fairy.y] == 0
			&& map[fairy.x + 1][fairy.y + 1] == 0) {
			fairy.x++;
			moveGol(fairy);
		}
		//왼쪽의 3칸이 모두 비어있는 경우 왼쪽으로 이동한다.
		else if(fairy.y - 2 >= 0 && fairy. x + 2 < r + 3 
				&& map[fairy.x - 1][fairy.y - 1] == 0 
				&& map[fairy.x][fairy.y - 2] == 0
				&& map[fairy.x - 1][fairy.y + 1] == 0
				
				&& map[fairy.x + 1][fairy.y - 2] == 0
				&& map[fairy.x + 2][fairy.y - 1] == 0) {
			
			//반시계 방향 회전 시킨다.
			fairy.reverseRotate();
			fairy.x++;
			fairy.y--;
			moveGol(fairy);
		
		//오른쪽으로 이동한다.
		}else if(fairy.y + 2 < c  && fairy.x + 2 < r + 3
				&& map[fairy.x - 1][fairy.y + 1] == 0
				&& map[fairy.x][fairy.y + 2] == 0
				&& map[fairy.x + 1][fairy.y + 1] == 0
				&& map[fairy.x + 1][fairy.y + 2] == 0
				&& map[fairy.x + 2][fairy.y + 1] == 0) {
			//시계방향 회전 시킨다.
				fairy.rotate();
				fairy.x++;
				fairy.y++;
				moveGol(fairy);	
		}
		return;
	}
	private static void init() throws IOException {
		br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer(br.readLine());
		
		r = Integer.parseInt(st.nextToken());
		c = Integer.parseInt(st.nextToken());
		k = Integer.parseInt(st.nextToken());
		idMap = new int[r+3][c];
		//숲 초기화
		map = new int[r + 3][c];
	}

}