import java.util.*;
import java.io.*;

public class Main {

	/*
	 * -기절, 격자 밖 산타 "탈락" 움직일 수 없다.;
	 * 
	 * #루돌프 움직임;
	 * - 루돌프는 탈락하지 않은 산타중 가장 가까운 산타에게 1칸 돌진;
	 * -- 만약 동일한 거리의 산타가 존재하면, r이 큰 산타, c가 큰 산타 우선순위; -> 변수 1
	 * -- 루돌프는 8방향중 하나로 이동할 수 있다. 대각선도 1칸이동으로 친다.;
	 * 
	 * #산타 움직임;
	 * -산타는 1번부터 ~P번 까지 순서대로 움직인다.;
	 * -기절 or 탈락한 산타는 움직일 수 없다.;
	 * -다른 산타가 있거나, 게임판 밖으로는 움직일 수 없다.;
	 * -움직일 수 없다면, 움직이지 않는다.;
	 * -루돌프와 가까워지는 곳이 없다면 산타는 움직이지 않는다.!!!;
	 * - 4방향 중 한 곳으로 움직인다.;
	 * - 거리가 동일한 곳이 여러개라면 (상, 우, 하, 좌 ) 우선순위에 맞게 움직인다.; -> point와 루돌프 인덱스를 기록할 변수
	 * 
	 * # 충돌;
	 * - 산타와 루돌프가 같은 자리에 있게되면 충돌이다.;
	 * - 루돌프의 움직임으로 인한 충돌 => 산타는 C의 점수를 얻는다.;
	 * 							=> 산타는 루돌프의 이동방향으로 C만큼 밀려난다.;
	 * - 산타의 움직임으로 인한 충돌 => 산타는 D의 점수를 얻는다.; 
	 * 							=> 산타는 이동해온 반대 방향으로 D만큼 밀려난다.
	 * - 충돌로 인한 움직임의 경우 원래 예정된 위치로 이동한다.;
	 * - 밀려난 위치가 "게임판 밖" => 산타 탈락;
	 * 				"다른 산타" => 상호 작용 방생;
	 * # 상호작용
	 * - 충돌 이후, 착지되는 칸에서만 상호작용이 일어난다;
	 * - 산타는 충돌 후 착지하게 되는 칸에 다른 산타가 있으면, 그 산타는 1칸 해당 방향으로 밀려난다.;
	 * 	=> 산타가 있으면 계속 반복;
	 * - 나가면 탈락.;
	 * 
	 * # 기절;
	 * - 산타는 루돌프와의 충돌 후 기절하게 된다.;
	 * - k턴에 충돌이 일어나면, k + 1턴은 아무것도할 수없고, k + 2부터 정상이 된다.;
	 * - 기절한 "산타"는 움직일 수 없지만, 충돌이나 상호작용에서는 밀려날 수 있다.
	 * - 루돌프는 기절한 산타를 대상으로 돌진할 수 있다.;
	 * 
	 * # 게임 종료;
	 * - M번의 턴의 걸쳐, 루돌프 산타가 순서대로 움직인 이후 종료;
	 * - P명의 산타가 모두 탈락하면 게임 종료;
	 * - 매턴 이후 아직 탈락하지 않은 산타들에게 1점 추가 부여;
	 * - 게임 종료 후 각 산타가 얻은 최종 점수를 구해야한다.!!;
	 * 
	 * */
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
	
	static class Info{
		
		int index;
		int r;
		int c;
		int dist;
		
		public Info(int index, int r, int c) {
			this.index = index;
			this.r = r;
			this.c = c;
		}
		
		

		public Info(int index, int r, int c, int dist) {
			this.index = index;
			this.r = r;
			this.c = c;
			this.dist = dist;
		}



		@Override
		public String toString() {
			return "Info [index=" + index + ", r=" + r + ", c=" + c + ", dist=" + dist + "]";
		}
	
	}
	
	static int n, m, p, c,d; //보드 크기, m게임횟수, p산타수, c가해자 루돌프, d가해자 산타
	
	static int[][] map; //합격
	static boolean[] outs; //루돌프의 탈락 여부
	static int[] stuns;//루돌프가 언제까지 기절 하는지
	static int[] scores;//점수
	static Point[] santas;//산타s 위치 [index] = "산태 현재 위치"
	static Point rudolf;;//루돌프 위치
	
	public static void main(String[] args) throws IOException {
		//초기화
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer(br.readLine());
		n = Integer.parseInt(st.nextToken());
		m = Integer.parseInt(st.nextToken());
		p = Integer.parseInt(st.nextToken());
		c = Integer.parseInt(st.nextToken());
		d = Integer.parseInt(st.nextToken());
		
		//map생성
		map = new int[n][n];
		//산타의 index는 1부터 시작한다. map에 표기하기 위해. 0은 아무것도 존재하지 않음!!!!
		outs = new boolean[n + 1];
		stuns = new int[n + 1];
		santas = new Point[n + 1];
		scores = new int[n + 1];
		//rudolf 초기 좌표
		st = new StringTokenizer(br.readLine());
		rudolf = new Point(Integer.parseInt(st.nextToken()) - 1, Integer.parseInt(st.nextToken()) - 1);
		map[rudolf.x][rudolf.y] = -1; //루돌프는 -1로 표기한다.
		
		for(int i = 0; i < p; i++) {
			st = new StringTokenizer(br.readLine());
			int index = Integer.parseInt(st.nextToken());
			int r = Integer.parseInt(st.nextToken()) - 1;
			int c = Integer.parseInt(st.nextToken()) - 1;
			santas[index] = new Point(r,c);
			map[r][c] = index;
		}
		/**/
		
		for(int i = 0; i < m; i++) {

			//1-1. 루돌프 이동 -> 충돌
			moveRudolf(i);
			//2-2. 산타 생존여부 파악.(산타가 모두 탈락이면 종료) 
			boolean isEnd = true;
			for(int j = 1; j <= p; j++) {
				if(!outs[j]) {
					isEnd = false;
				}
			}
			if(isEnd) break;
			//2-3. 산타 이동 -> 기절, 탈락 여부 판단
			moveSantas(i);
			//3. 생존산타 점수 계산
			calScores();
		}
		
		StringBuilder sb = new StringBuilder();
		for(int i = 1; i <= p; i++) {
			if(i != p) {
				sb.append(scores[i]+" ");
			}else {
				sb.append(scores[i]);
			}
		}
		System.out.println(sb.toString());
		//1.m번 반복
		//1-1. 루돌프 이동 -> 충돌
		//2-2. 산타 생존여부 파악.(산타가 모두 탈락이면 종료) 
		//2-3. 산타 이동 -> 충돌
		//3.생존산타 점수 계산
		
	}
	
	private static void calScores() {
		for(int i = 1; i <=p ; i++) {
			if(outs[i]) continue;
			scores[i]++;
		}
	}

	private static void moveSantas(int round) {
	
	/*
	 *  
	 * #산타 움직임;
	 * -산타는 1번부터 ~P번 까지 순서대로 움직인다.;
	 * -기절 or 탈락한 산타는 움직일 수 없다.;
	 * -다른 산타가 있거나, 게임판 밖으로는 움직일 수 없다.;
	 * -움직일 수 없다면, 움직이지 않는다.;
	 * -루돌프와 가까워지는 곳이 없다면 산타는 움직이지 않는다.!!!;
	 * - 4방향 중 한 곳으로 움직인다.;
	 * - 거리가 동일한 곳이 여러개라면 (상, 우, 하, 좌 ) 우선순위에 맞게 움직인다.; -> point와 루돌프 인덱스를 기록할 변수
	  */
		
		int dx[] = {-1,0,1,0};
		int dy[] = {0,1,0,-1};
		for(int i = 1;  i <= p; i++) {
			if(outs[i]) continue; //탈락한 경우
			if(stuns[i] > round) continue; //기절한 경우
			
			Point santa = santas[i];
			//현재 산타를 이동시킨다.
			int minDist = (int)Math.pow(santa.x - rudolf.x, 2) + (int)Math.pow(santa.y - rudolf.y, 2); 
			int nextX = -1;
			int nextY = -1;
			int dir = 0;
			//1)상 하 좌 우를 모두 이동시켜본다.
			for(int j = 0;  j < 4; j++) {
				int nx = santa.x + dx[j];
				int ny = santa.y + dy[j];
				if(isOnRange(nx,ny) && (map[nx][ny] == 0 || map[nx][ny] == -1)) {
					int dist = (int)Math.pow(nx - rudolf.x, 2) + (int)Math.pow(ny - rudolf.y, 2); 
					if(dist < minDist) {
						minDist = dist;
						nextX = nx;
						nextY = ny;
						dir = j;
					}
				}
			}
			//이동이 불가능한 경우
			if(nextX == -1 && nextY ==-1) {
				continue;
			}
			
			//산타 기존자리 0으로
			map[santa.x][santa.y] = 0;
			//이돌할 자리에 루돌프가 있는 경우 -> 충돌			
			if(map[nextX][nextY] == -1) {
				//1)기절
				stuns[i] = round + 2;
				//2)점수획득
				scores[i] += d;
				dir = (dir + 2) % 4;
				int sx = nextX + (dx[dir] * d);
				int sy = nextY + (dy[dir] * d);
				if(isOnRange(sx,sy)) {
					//탈락하지 않은 경우
					Queue<Info> q = new LinkedList<>();
					q.add(new Info(i, sx, sy));
					
					while(!q.isEmpty()) {
						Info now = q.poll();
						int nextIndex = map[now.r][now.c];
						map[now.r][now.c] = now.index;
						santas[now.index] = new Point(now.r, now.c);
						if(nextIndex != 0) {
							int nx = now.r + dx[dir];
							int ny = now.c + dy[dir];
							if(!isOnRange(nx,ny)) {
								//탈락로직
								//1)탈락표기 2)map에서 삭제
								outs[nextIndex] = true;
								continue;
							}
							q.add(new Info(nextIndex,nx,ny));
						}
						
						
					}
				}else {
					//탈락
					outs[i] = true;
				}
					
				
			}else {
				//루돌프도 없고 산타도 없음
				//기존산타 자리 0
				map[nextX][nextY] = i;
				santas[i] = new Point(nextX,nextY);
			}
			
			//	-> 이동한 좌표에 루돌프가 있는 경우 충돌한다.
			// 	-> 충돌한 경우 이동했던 방향과 반대로 움직인다. BFS
		}
		
	}

	private static void moveRudolf(int round) {
	/* #루돌프 움직임;
	 * - 루돌프는 탈락하지 않은 산타중 가장 가까운 산타에게 1칸 돌진; -> 시간복잡도 괜찮나? 가능
	 * -- 만약 동일한 거리의 산타가 존재하면, r이 큰 산타, c가 큰 산타 우선순위; -> 변수 1
	 * -- 루돌프는 8방향중 하나로 이동할 수 있다. 대각선도 1칸이동으로 친다.;
	 */
		//1.가장 가까운 산타찾기
		PriorityQueue<Info> pq = new PriorityQueue<>((o1,o2)-> {
			if(o1.dist!= o2.dist) {
				return o1.dist - o2.dist;
			}else if(o1.r != o2.r) {
				return o2.r - o1.r;
			}else {
				return o2.c - o1.c;
			}
		});
		
		//1-1.map을 순회하며 0이 아닌 산타의 인덱스와, 해당 좌표를 pq에 넣는다.
		for(int i = 0; i < n; i++) {
			for(int j = 0; j < n; j++) {
				if(map[i][j] == 0 || map[i][j] == -1) {
					continue;
				}
				int dist = (int)Math.pow(i - rudolf.x, 2) + (int)Math.pow(j - rudolf.y, 2); 
				pq.add(new Info(map[i][j],i,j,dist));
			}
		}
	
		//1-2.pq에서 우선순위가 가장 높은 거 하나 뺀다.
		if(pq.isEmpty()) return;
		Info selected = pq.poll();
		//2.8방향 중 우선순위가 가장 높은 곳으로 가까워 지는 방향으로 1칸 이동시킨다.
		//현재 루돌프의 위치에 + 8방향을 한 값중 거리가 가장 작은 쪽으로 한칸 이동
		int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
		int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};
		int minDist = Integer.MAX_VALUE;
		int nextX = -1;
		int nextY = -1;
		int dir = -1;
		for(int i = 0; i < 8; i++) {
			int nx = rudolf.x + dx[i];
			int ny = rudolf.y + dy[i];
			if(isOnRange(nx,ny)) {
				int dist = (int)Math.pow(nx - selected.r, 2) + (int)Math.pow(ny - selected.c, 2); 
				if(dist < minDist) {
					minDist = dist;
					nextX = nx;
					nextY = ny;
					dir = i;
				}
			}
		}
		//3.이동한 좌표에 산타가 존재하면 -> 충돌! -> 점수 표기 , stuns에 현재 라운드 + 2 표기.
		//BFS를 통해 이동해야하는 방향을 더해서 Q에 넣어준다.
		//-bfs에서 맵을 벗어난 경우! 탈락이다.	
		int crushedSantaIndex = map[nextX][nextY];
		//루돌프 위치 변경
		map[rudolf.x][rudolf.y] = 0;
		map[nextX][nextY] = -1;
		rudolf.x = nextX;
		rudolf.y = nextY;
		if(crushedSantaIndex != 0) {
			//충돌이 발생한 경우
			//1) 기절 stuns에 현재 라운드 +2 표기
			stuns[crushedSantaIndex] = round + 2;
			//2) 점수 계산.
			scores[crushedSantaIndex] += c;
			//3) 연쇄작용 테스트 및 루돌프 위치 고정
			int nx = nextX + (dx[dir] * c);
			int ny = nextY + (dy[dir] * c);
			//구간에 존재하는 경우
			if(isOnRange(nx,ny)) {
				Queue<Info> q = new LinkedList<>();
				q.add(new Info(crushedSantaIndex, nx, ny));
				while(!q.isEmpty()) {
					Info now = q.poll();
					//현재 위치가 벗어난 경우 탈락
					//현재 위치에 다른 산타가 있으면 추가한다.
					int nextIndex = map[now.r][now.c];
					map[now.r][now.c] = now.index;
					santas[now.index] = new Point(now.r,now.c);
					if(nextIndex != 0) {
						int nr = now.r + dx[dir];
						int nc = now.c + dy[dir];
						if(!isOnRange(nr,nc)) {
							//탈락 로직
							//1)탈락표기 2)map에서 삭제
							outs[nextIndex] = true;
							continue;
						}
						q.add(new Info(nextIndex,nr,nc));
					}
				}
			} else {
				// 탈락!
				outs[crushedSantaIndex] = true;
			}
			
		}
	}

	/**
	 * map을 벗어나지 않는지 확인하는 메서드
	 * @param x
	 * @param y
	 * @return 
	 */
	private static boolean isOnRange(int x, int y) {
		if(x >= 0 && x < n && y >= 0 && y < n) {
			return true;
		}
		return false;
	}

	private static void printMap() {
	
		for(int i = 0; i < n; i++) {
			for(int j =0 ; j < n; j++) {
				System.out.print(map[i][j]+" ");
			}
			System.out.println();
		}
		
		System.out.println("=== === === === ===");
	}

}