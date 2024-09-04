import java.io.*;
import java.util.*;

public class Main {
	/*
	 * 아래의 과정을 k번 반복한다.;
	 * -좌표선정;
	 * 	0.map을 깊은 복사한 배열로 실행한다.
	 * 	1.(1, 1) ~ (3, 3)을 중심좌표로 설정한다.;
		 * 	2. 90, 180, 270를 회전시킨다.;
		 * 	3. 회전 시킨 이후, 각 좌표에서 1차 획득점수를 계산한다.
		 *  4. 계산한 시점의 중심좌표, 획득가치, 회전각도르 저장한다. => ArrayList<Info>;
	 * 
	 * 	
	 * 	4. 우선순위가 높은 중심좌표와, 회전각도를 찾는다.;
	 *		우선순위;
	 *		1) 1차 획득가치 큰거;
	 *		2) 작은 회전각도 작은 거;
	 *		3) 회전 중심의 열이 작다.;
	 *		4) 회전 중심의 열이 작다.;
	 *	--이때, 1차 획득점수가 전부 0인 경우 K번 반복하지 않고 종료한다.!!!!;
	 *-점수 계산
	 * 1. 회전 중심과 회전 각도로 회전시킨다.;	
	 * 2. 점수를 측정한다.;
	 * 	- 연속되는 값이 있는지 확인한다.-> 없으면 종료한다.
	 * 	- 3개 이상으로 연결된 유물좌표를 기록한다. ArrayList<Point> conPoints로 기록
	 * 	- 값을 채워야하는 우선순위대로 정렬!
	 * 	- 새로 값을 채운다.
	 * 	- conPoints를 점수에 더한다.
	 * 		
	 * 	
	 * 
	 *-유물 연쇄 획득;
	 *	1. 빈칸에 우선순위대로 값을 채워넣는다.;
	 *	우선순위:;
	 *		빈칸의 좌표의 열이 작은순,
	 *		빈칸의 좌표의 행이 큰순,
	 *	2. 점수를 계산한다.
	 *	0. 
	 * */
	static int k;//탐사 반복 횟수
	static int m;//유물 조각의 개수
	static Queue<Integer> spares;//m개의 충전용 유물
	static int[][] map;// 5 x 5크기를 갖는 맵
	static StringBuilder sb;
	static boolean isEnd; 
	static int[] dx = {-1,1,0,0};
	static int[] dy = {0,0,-1,1};
	static List<Point> points; //자리를 채워야하는 좌표들
  	static class Info implements Comparable<Info>{
		int value; //획득한 가치
		int degree; //회전 각도
		Point point; //중심좌표
		
		public Info(int value, int degree, Point point) {
			this.value = value;
			this.degree = degree;
			this.point = point;
		}
		
		

		@Override
		public int compareTo(Info o) {
			if(this.value != o.value) {
				return -1 * Integer.compare(this.value, o.value);
			}else if(this.degree != o.degree) {
				return Integer.compare(this.degree, o.degree);
			}else if(this.point.y != o.point.y) {
				return Integer.compare(this.point.y, o.point.y);
			}
			return Integer.compare(this.point.x, o.point.x);
		}


		@Override
		public String toString() {
			return "Info [value=" + value + ", degree=" + degree + ", point=" + point + "]";
		}
		
		
	}
	static class Point implements Comparable<Point>{
		int x;
		int y;
		
		public Point(int x, int y) {
			super();
			this.x = x;
			this.y = y;
		}

		@Override
		public int compareTo(Point o) {
			if(this.y != o.y) {
				return this.y - o.y;
			}
			return -1 * (this.x - o.x);
		}

		@Override
		public String toString() {
			return "Point [x=" + x + ", y=" + y + "]";
		}
		
	}
	public static void main(String[] args) throws IOException{
		init();
		for(int i = 0; i < k; i++) {
			if(isEnd) break; //isEnd라는 변수가 true가 되면 종료한다.
			solve();
		}
		System.out.println(sb);
	}
	private static void solve() {
		/*
		 * -좌표선정;
		 * 	
		 * 	1.(0, 0) ~ (2, 2)을 중심좌표로 설정한다.;
		 * 		1. map을 깊은 복사한 배열로 실행한다.
			 * 	2. 90, 180, 270를 회전시킨다.;
			 * 	3. 회전 시킨 이후, 각 좌표에서 1차 획득점수를 계산한다.
			 *  4. 계산한 시점의 중심좌표, 획득가치, 회전각도르 저장한다. => ArrayList<Info>;
		 * 
		 */
		
		//5 * 5를 맵은 변화하면 안된다.
		ArrayList<Info> infos = new ArrayList<>();
		for(int i = 0;  i <= 2 ; i++) {
			for(int j = 0;  j <= 2; j++) {
				//map을 깊은 복사한 배열로 실행한다.
				int[][] map33 = copyMap33(map, i, j);
				//90도, 180도, 270도를 회전한다.
				for(int r = 0; r < 3; r++) {
					rotate(map33);
					points = new ArrayList<>();
					int count = findContinuosU(map33, i, j);
					infos.add(new Info(count, r, new Point(i,j)));
//					System.out.println("added info="+infos.getLast());
				}
			}
		}


		//우선순위가 가장 높았던 값을 뽑는다.
		Collections.sort(infos);
		Info bestInfo = infos.get(0);
//		System.out.println("bestInfo="+ bestInfo);
		//아무것도 없으면 종료한다.
		if(bestInfo.value == 0) {
//			System.out.println("isEnd true");
//			System.out.println("infos="+infos);
			isEnd = true;
			return;
		}
		
		
		//map에 반영한다.
		int[][] map33 = copyMap33(map, bestInfo.point.x, bestInfo.point.y);
		for(int i = 0;  i <= bestInfo.degree; i++) {
			rotate(map33);
		}
		
		for(int i = 0; i < 3; i++) {
			for(int j = 0;  j < 3; j++) {
				map[bestInfo.point.x + i][bestInfo.point.y + j] = map33[i][j];
			}
		}
		
//		printMap(map,"map!!");
		//점수 세기
		int total = 0;
		int count = 0;
		boolean[][] visited = new boolean[5][5];
		points = new ArrayList<>();
		for(int i = 0; i < 5; i++) {
			for(int j = 0; j < 5; j++) {
				if(visited[i][j]) continue;
				 count += bfs(i,j,visited, map);
			}
		}
		while(count != 0) {
			total += count;
			//새로운 값을 채운다.
			Collections.sort(points);
//			System.out.println("count="+count);
//			System.out.println("points=" + points);
			for(Point point : points) {
//				System.out.println("spares="+ spares);
				map[point.x][point.y] = spares.poll();
			}
			
			count = 0;
			visited = new boolean[5][5];
			points = new ArrayList<>();
			for(int i = 0; i < 5; i++) {
				for(int j = 0; j < 5; j++) {
					if(visited[i][j]) continue;
					 count += bfs(i,j,visited, map);
				}
			}
			
//			printMap(map,"afterMap");
		}
//		System.out.println("after spares="+spares);
//		System.out.println("total="+ total);
		sb.append(total+" ");
		
	}
	private static int[][] copyMap(int[][] origin) {
		int[][] copy = new int[origin.length][origin[0].length];
		for(int i = 0 ; i < origin.length; i++) {
			copy[i] = origin[i].clone();
		}
		return copy;
	}
	//연속되는 개수의 좌표를 기록한다.
	private static int findContinuosU(int[][] map33, int sx, int sy) {
		//map
		int[][] tempMap = new int[5][5];
		for(int i = 0; i < 5; i++) {
			tempMap[i] = map[i].clone();
		}
		
		for(int i = 0;  i< 3; i++) {
			for(int j = 0;  j < 3; j++) {
				tempMap[sx+i][sy+j] = map33[i][j];
			}
		}
		//개수 세야한다. bfs
		int count = 0;
		boolean[][] visited = new boolean[5][5];
		for(int i = 0; i < 5; i++) {
			for(int j = 0; j < 5; j++) {
				if(visited[i][j]) continue;
				count += bfs(i,j,visited, tempMap);
			}
		}
		return count;
	}
	
	
	private static int bfs(int sx, int sy , boolean[][] visited, int[][] tempMap) {
		
		ArrayList<Point> temps = new ArrayList<>();
		visited[sx][sy] = true;
		Queue<int[]> q= new LinkedList<>();
		q.add(new int[] {sx, sy, 1});
		
		int value = tempMap[sx][sy];
		int count = 0;
		while(!q.isEmpty()) {
			int[] now = q.poll();
			temps.add(new Point(now[0],now[1]));
			count++;
			for(int dir = 0;  dir < 4; dir++) {
				int nx = now[0] + dx[dir];
				int ny = now[1] + dy[dir];
				
				if(nx < 0 || nx >= 5 || ny < 0 || ny >= 5 || visited[nx][ny] || tempMap[nx][ny] != value ) {
					continue;
				}
				visited[nx][ny] = true;
				q.add(new int[] {nx ,ny ,now[2] + 1});
			}
		}
		if(count >= 3) {
			points.addAll(temps);
//			System.out.println("temps="+temps);
//			System.out.println("count in bfs=" + count);
			return count;
		}
		return 0;
	}
	//3곱하기 3으로 회전한다. 
	private static void rotate(int[][] origin) {
		int[][] rotateMap = new int[origin.length][origin[0].length];
		//회전
		for(int i = 0;  i < origin.length; i++) {
			for(int j = 0; j < origin.length; j++) {
				rotateMap[i][j] = origin[origin.length - j - 1][i];
			}
		}

		for(int i = 0;  i < origin.length; i++) {
			for(int j = 0; j < origin.length; j++) {
				origin[i][j] = rotateMap[i][j];
			}
		}
	}
	private static void printMap(int[][] map, String str) {
		System.out.println(str);
		for(int i = 0;  i < map.length; i ++) {
			for(int j = 0; j < map[0].length; j++) {
				System.out.print(map[i][j] +" ");
			}
			System.out.println();
		}
		System.out.println("========");
	}
	private static int[][] copyMap33(int[][] origin, int sx, int sy) {
		int[][] copy = new int[3][3];
		for(int i = 0 ; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				copy[i][j] = origin[sx + i][sy + j];
			}
		}
		return copy;
	}
	private static void init() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer(br.readLine());
		sb = new StringBuilder();
		
		k = Integer.parseInt(st.nextToken());//탐사회수
		m = Integer.parseInt(st.nextToken());//벽면유적개수 spares
		
		map = new int[5][5];
		spares = new LinkedList<>();//m개의 벽면 유적개수를 저장할 q
		
		//기본격자를 입력받는다.
		for(int i = 0; i < 5; i++) {
			st = new StringTokenizer(br.readLine());
			for(int j = 0; j < 5 ; j++) {
				map[i][j] = Integer.parseInt(st.nextToken());
			}
		}
		
		//spare를 입력받는다. 벽면유적개수
		st = new StringTokenizer(br.readLine());
		for(int i = 0;  i < m; i++) {
			spares.add(Integer.parseInt(st.nextToken()));
		}
	}

}