import java.io.*;
import java.util.*;

public class Main {
	
	/*
	 * 1.루돌프 움직임 
	 * --루돌프는 거리가 가장 가까운 산타를 향해 1칸 이동한다.(거리가 가까운순서, r이 큰 산타, c가 큰 산타)
	 * ===>comparator사용해서 구현하기 int[4] /거리,r,c,index를 넣고 하나만 추린다. 
	 * --루돌프는 8방향으로 움직일 수 있다.
	 * => 충돌, 상호작용
	 * 2.산타들 움직임(순서대로)
	 * --산타는 index 순서대로 움직인다.
	 * --기절했거나, 탈락한 산타는 움직일 수 없다.
	 * --루돌프와 가까워지는 방향으로 1칸이동한다. (상 우 하 좌)우선순위
	 * --산타는 다른 산타가 존재하는 칸이나, 게임장 밖으로 움직일 수 없다.
	 * => 충돌
	 * 
	 * ###충돌 -> 기절
	 * 산타와 루돌프는 같은 칸에 있게되면 충돌이 발생한다.
	 * 충돌하게되면 산타는 기절하게 된다. 다음 턴 까지 행동불가.
	 * # 루돌프에 의한 충돌
	 * --산타는 C점을 획득한다.
	 * --산타는 C만큼 루돌프의 이동방향으로 밀려난다.
	 * 
	 * # 산타에 의한 충돌
	 * --산타는 D만큼 점수를 얻는다.
	 * --산타는 자신이 이동해온 반대방향으로 D만큼 밀려난다.
	 * 
	 * #common 
	 * -- 밀려난 곳이 게임장 밖이면 산타는 탈락한다.
	 * -- 밀려난 곳에 다른 산타가 있으면 '상호작용'이 발생한다.
	 * => 상호작용
	 * 
	 * ###상호작용
	 * -- 충돌후 착지하는 칸에 다른 산타가 존재하면, 해당방향으로 1칸 밀려난다.
	 * -- 연쇄적으로 밀려나는 것을 반복하며 게임판 밖으로 밀려나오게 된 산타의 경우 게임에서 탈락한다.
	 * */
	
	static int n,m,p;//게임판의크기 , 게임턴 수, 산타 수
	static int c,d;//루돌프힘, 산타힘
	static int[] stuns;//stun여부
	static boolean[] outs;//탈락여부
	static int[][] map;//산타의 위치
	static Santa[] santas;//산타의 정보들(인덱스,x,y);
	static Rudolf rudolf;//루돌프
	static int[] score;
	static int round;
	static class Rudolf{
		int x;
		int y;
		
		public Rudolf(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		@Override
		public String toString() {
			return "Rudolf [x=" + x + ", y=" + y + "]";
		}
	}
	static class Santa{
		int index;
		int x;
		int y;
		
		public Santa(int index, int x, int y) {
			this.index = index;
			this.x = x;
			this.y = y;
		}

		@Override
		public String toString() {
			return "Santa [index=" + index + ", x=" + x + ", y=" + y + "]";
		}
	}
	public static void main(String[] args) throws IOException {
		init();
		//TODO
//		System.out.println("입력값 테스트");
//		printMap();
		solve();
		printScore();
	}
	private static void printScore() {
		for(int i = 1; i <= p; i++) {
			System.out.print(score[i] +" ");
		}
		System.out.println();
		
	}
	private static void solve() {
		//TODO M으로 교체
		for(int i = 1; i <= m; i++) {
			round = i;
			moveRudolf();
//			System.out.println("round= " + i + "루돌프 움직임 이후");
//			printMap();
			//산타움직임
			moveSanta();
//			System.out.println("round= " + i + "산타 움직임 이후");
//			printMap();
			//점수 계산
			addAliveScore();
//			System.out.println("round= " + i + "라운드 종료 후 점수");
//			printScore();
			//종료 여부 판단
			if(end()) {
				return;
			}
		}
	}
	
	private static boolean end() {
		int cnt = 0;
		for(int i = 1; i<= p; i++) {
			if(!outs[i]) cnt++;
		}
		
		if(cnt == 0) return true;
		return false;
	}
	private static void addAliveScore() {
		for(int i = 1; i <= p; i++) {
			if(outs[i]) continue;
			score[i]++;
		}
		
	}
	private static void moveSanta() {
		
		int[] dx = {-1,0,1,0};
		int[] dy = {0,1,0,-1};

	
		for(int i = 1; i<= p; i++) {
			if(stuns[i] >= round || outs[i]) continue;
			Santa santa = santas[i];
			//루돌프와 가까워지는 방향으로 1칸 이동한다.
			int minDist = calDist(rudolf.x, rudolf.y, santa.x, santa.y);
			int mdx = 0;
			int mdy = 0;
			for(int dir = 0; dir < 4; dir++) {
				int nx = santa.x + dx[dir];
				int ny = santa.y + dy[dir];
				if(!isOnRange(nx,ny) || map[nx][ny] > 0) continue;
				int dist = calDist(rudolf.x, rudolf.y, nx, ny);
				
				//거리가 더 짧은 경우 해당 방향으로 이동
				if(dist < minDist) {
					minDist = dist;
					mdx = dx[dir];
					mdy = dy[dir];
				}
			}
			
			
			//원래자리 비우기
			map[santa.x][santa.y] = 0;
			int snx = santa.x + mdx;
			int sny = santa.y + mdy;
			
			//루돌프랑 충돌한 경우
			if(snx == rudolf.x && sny == rudolf.y) {
				stuns[santa.index] = round + 1;
				score[santa.index] += d;
				interAction(santa.index, -1 * mdx, -1 * mdy, d);
			}else {
			//충돌하지 않은 경우
				map[snx][sny] = santa.index;
				santas[santa.index].x = snx;
				santas[santa.index].y = sny;
			}
			
		}
				
		
		
//		 * 2.산타들 움직임(순서대로)
//		 * --산타는 index 순서대로 움직인다.
//		 * --기절했거나, 탈락한 산타는 움직일 수 없다.
//		 * --루돌프와 가까워지는 방향으로 1칸이동한다. (상 우 하 좌)우선순위
//		 * --산타는 다른 산타가 존재하는 칸이나, 게임장 밖으로 움직일 수 없다.
//		 * => 충돌
	}
	private static void moveRudolf() {
		PriorityQueue<List<Integer>> pq = new PriorityQueue<>((o1,o2) ->{
			if(o1.get(0) != o2.get(0)) {
				return Integer.compare(o1.get(0), o2.get(0));
			}
			
			if(o1.get(1) != o2.get(1)) {
				return -1 * Integer.compare(o1.get(1), o2.get(1));
			}
			
			return -1 * Integer.compare(o1.get(2), o2.get(2));
		});
		
		
		//필드위의 산타와 루돌프와의 거리를 구해서 pq에 넣음
		for(int i = 1; i<=p; i++) {
			if(outs[i]) continue;
			Santa santa = santas[i];
			int dist = calDist(rudolf.x, rudolf.y, santa.x, santa.y);
			pq.add(Arrays.asList(dist,santa.x,santa.y,santa.index));
		}
		
		int tIndex = pq.poll().get(3); //targetIndex
		
		/* 해당 targetIndex루돌프를 향해가장 가까운 칸으로 이동함. */
		//좌상, 상 , 우상, 좌, 우, 좌하, 하, 우하
		int[] dx = {-1,-1,-1,0,0,1,1,1};
		int[] dy = {-1,0,1,-1,1,-1,0,1};
		//거리, 방향
		PriorityQueue<List<Integer>> npq = new PriorityQueue<>((o1,o2)-> Integer.compare(o1.get(0), o2.get(0)));
		
		for(int i = 0; i < 8; i++) {
			int nx = rudolf.x + dx[i];
			int ny = rudolf.y + dy[i];
			
			if(!isOnRange(nx,ny))continue;
			int dist = calDist(nx,ny,santas[tIndex].x, santas[tIndex].y);
			npq.add(Arrays.asList(dist, i));
		}
		
		
//		System.out.println(npq);
		List<Integer> dirs = npq.poll();
		int dir = dirs.get(1);
		int ndx = dx[dir];
		int ndy = dy[dir];
		
		map[rudolf.x][rudolf.y] = 0;
		rudolf.x += ndx;
		rudolf.y += ndy;
		
		int crushSantaIndex = map[rudolf.x][rudolf.y];
		map[rudolf.x][rudolf.y] = -1;
		
//		printMap();
		//충돌한경우, 충돌 후, -> 상호작용
		if(crushSantaIndex != 0) {
			//점수 부여
			score[crushSantaIndex] += c;
			//stun
			stuns[crushSantaIndex] = round + 1;
			//상호 작용
			interAction(tIndex, ndx, ndy, c);
		}
		
		
//		* 1.루돌프 움직임 
//		 * --루돌프는 거리가 가장 가까운 산타를 향해 1칸 이동한다.(거리가 가까운순서, r이 큰 산타, c가 큰 산타)
//		 * ===>comparator사용해서 구현하기 int[4] /거리,r,c,index를 넣고 하나만 추린다. 
//		 * --루돌프는 8방향으로 움직일 수 있다.
//		 * => 충돌, 상호작용
		
	}
	private static void interAction(int index, int dx, int dy, int dist) {
		Queue<Santa> q = new LinkedList<>();
		int sx = rudolf.x + (dx * dist);
		int sy = rudolf.y + (dy * dist);
	
		if(!isOnRange(sx,sy)) {
			outs[index] = true;
			return;
		}
		
		q.add(new Santa(index, sx, sy));
		
		while(!q.isEmpty()) {
			Santa now = q.poll();
			int nextIndex = map[now.x][now.y];
			map[now.x][now.y] = now.index;
			santas[now.index].x = now.x;
			santas[now.index].y = now.y;
			
			if(nextIndex != 0) {
				int nx = now.x + dx;
				int ny = now.y + dy;
				//벗어난 경우
				if(!isOnRange(nx,ny)) {
					outs[nextIndex] = true;
					continue;
				}
				//벗어나지 않은 경우
				q.add(new Santa(nextIndex, nx, ny));
			}
		}
	}
	private static boolean isOnRange(int x, int y) {
		if(x < 0 || y < 0 || x >= n || y >= n) {
			return false;
		}
		return true;
	}
	
	private static int calDist(int x, int y, int x2, int y2) {
		return (int)(Math.pow(x-x2,2) + Math.pow(y - y2, 2));
	}
	private static void init() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer(br.readLine());
		n = Integer.parseInt(st.nextToken());
		m = Integer.parseInt(st.nextToken());
		p = Integer.parseInt(st.nextToken());
		c = Integer.parseInt(st.nextToken());
		d = Integer.parseInt(st.nextToken());
		
		stuns = new int[p + 1];
		outs = new boolean[p + 1];
		map = new int[n][n]; //입력받은 좌표는 다 -1씩 깎아서 저장할것
		santas = new Santa[p + 1];
		score = new int[p + 1];
		//루돌프 좌표
		st = new StringTokenizer(br.readLine());
		int rx = Integer.parseInt(st.nextToken())-1;
		int ry = Integer.parseInt(st.nextToken())-1;
		map[rx][ry] = -1;
		rudolf = new Rudolf(rx,ry);
		
		//산타
		for(int i = 0; i < p; i++) {
			st = new StringTokenizer(br.readLine());
			int index = Integer.parseInt(st.nextToken());
			int x = Integer.parseInt(st.nextToken()) - 1;
			int y = Integer.parseInt(st.nextToken()) - 1;

			santas[index] = new Santa(index, x, y);
			map[x][y] = index;
		}
		
	}
	private static void printMap() {
		
		for(int i = 0; i < n; i++) {
			for(int j = 0; j < n; j++) {
				System.out.print(map[i][j]+" ");
			}
			System.out.println();
		}
		
		for(int i = 1; i<=p; i++) {
			System.out.print(santas[i]);
			System.out.println(" stun="+stuns[i]+" outs="+outs[i]);
		}
	}

}