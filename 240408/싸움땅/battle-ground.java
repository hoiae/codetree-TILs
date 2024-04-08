import java.io.*;
import java.util.*;

public class Main {

	/*
	 * 플레이어 순서대로 이동
	 * 	1. 진행방향으로 한칸 이동 -> 맵을 벗어나는 경우 정반대 방향으로 1칸 이동한다.
	 * 		1-1. 본인 총과, map[][]의 총을 비교하여 큰 것을 챙김
	 * 		1-2. 플레이어인 경우, 대결
	 * 			play+gun이 큰 순서, player가 큰 사람이 이김 , (차이만큼 승자가 점수를 챙김 score)
	 * 			a. 진 사람은 총이 존재하면, 해당 자리에 내려놓음
	 * 				진행 방향으로 한칸 이동함. -> 다른 사람이 있는 경우, 격자 밖인 경우, while오른쪽으로 90도씩 회전하며 빈칸이 존재하며 이동함.
	 * 				총들이 존재하면 가장 큰 총을 획득함.
	 * 			b. 이긴 사람은 현 위치에 있는 총들 중 가장 데미지가 센총을 획득하고, 다른 총들은 내려놓는다.
	 * */
	
	static int[] scores; //플레이어별 점수
	static PriorityQueue<Integer>[][] gunMap;//자리에 놓여진 총들을 기록하는 맵
	static Player[] players;
	static int[][] pMap;//플레이어의 인덱스를 기록하는 맵.
	static int N,M,K;// 격자크기, 플레이어 수, 라운드수
	static int[] dx = {-1,0,1,0};
	static int[] dy = {0,1,0,-1};
	static class Player implements Comparable<Player>{
		int index; //인덱스
		int x;
		int y;		
		int d; //방향
		int s; //기본 데미지
		int gun; //보유한 총 0인 경우 안들고 있는 것
		


		public Player(int index, int x, int y, int d, int s, int gun) {
			this.index = index;
			this.x = x;
			this.y = y;
			this.d = d;
			this.s = s;
			this.gun = gun;
		}


		@Override
		public int compareTo(Player o) {
			if(this.s + this.gun != o.s + o.gun) {
				return -1 * Integer.compare(this.s + this.gun, o.s + o.gun);
			}
			return -1 * Integer.compare(this.s, o.s);
		}
		


		@Override
		public String toString() {
			return "Player [index=" + index + ", x=" + x + ", y=" + y + ", d=" + d + ", s=" + s + ", gun=" + gun + "]";
		}
		
		
	}
	public static void main(String[] args) throws IOException {
		init();
		for(int i = 1; i<= K; i++) {
//			System.out.println(" round=" + i+ i+ i+ i+ i+ i+ i);
			solve();	
//			System.out.println(Arrays.toString(scores));
		}
		
		for(int i = 1; i <= M; i++) {
			System.out.print(scores[i]+" ");
		}
	
	}
	private static boolean isOnRange(int x, int y) {
		if(x < 0 || y < 0 || x >= N || y >= N) {
			return false;
		}
		
		return true;
	}
	private static void solve() {
		
		for(int i = 1; i <= M; i++) {
			Player now = players[i];
			pMap[now.x][now.y] = 0;

			//한칸 이동한다. -> pmap과 gunMap,과 player에 반영해야함.
			int nx = now.x + dx[now.d];
			int ny = now.y + dy[now.d];
			
			//벽을 벗어나는 경우 반대방향으로 이동한다.
			if(!isOnRange(nx,ny)) {
				now.d = (now.d + 2) % 4;
				nx = now.x + dx[now.d];
				ny = now.y + dy[now.d];
			}
			
			
			//이동하는 곳에 플레이어가 없는 경우 -> 위치를 변경해줘야함. 총이 있는 경우
			if(pMap[nx][ny] == 0) {
				//위치 정보 갱신
				pMap[nx][ny] = i;
				now.x = nx;
				now.y = ny;
				
				//총을 교환해야한다. 
				if(!gunMap[nx][ny].isEmpty()) {
					if(now.gun != 0) {
						if(now.gun < gunMap[nx][ny].peek()) {
							gunMap[nx][ny].offer(now.gun);
							now.gun = gunMap[nx][ny].poll();
						}
					}else {
						now.gun = gunMap[nx][ny].poll();
					}
					
				}
				
				
//				int gun = 0;
//				if(!gunMap[nx][ny].isEmpty()) {
//					gun = gunMap[nx][ny].poll();
//				}
//				
//				if(now.gun < gun) {
//					if(now.gun != 0) {//총을 들고 있지 않은 경우
//						gunMap[nx][ny].offer(now.gun);
//					}
//					now.gun = gun;
//				}
			//이동하는 곳에 플레이어가 있는 경우
			}else {
				
				//대결
				int otherPlayerIndex = pMap[nx][ny];
				Player[] vers = new Player[2];
				vers[0] = now;
				vers[1] = players[otherPlayerIndex];
				
				//0번째가 이긴것, 1번째 진것
				Arrays.sort(vers);
				Player loser = vers[1];
				Player winner = vers[0];

				//점수 계산
				int addScore = winner.gun + winner.s - (loser.gun + loser.s);
				scores[winner.index] += addScore;
				
				//진사람은 총을 해당 자리에 내려놓고, 떠난다.
				if(loser.gun != 0) {
					gunMap[nx][ny].add(loser.gun);
					loser.gun = 0;
				}
				//이동한다.
				while(true) {
					int loserNx = nx+ dx[loser.d];
					int loserNy = ny+ dy[loser.d];
					if(isOnRange(loserNx,loserNy) && pMap[loserNx][loserNy] == 0) {
						//이동완료 -> 좌표 반영, 총 갱신
						pMap[loserNx][loserNy] = loser.index;
						loser.x = loserNx;
						loser.y = loserNy;
						
						if(!gunMap[loserNx][loserNy].isEmpty()) {
							loser.gun = gunMap[loserNx][loserNy].poll();
						}
						
						break;
					}
					loser.d = ++loser.d % 4;
				}
				//이긴사람은 현위치의 총중 가장 센총을 획득하고, 다른 총은 내려놓는다.
				if(!gunMap[nx][ny].isEmpty()) {
					if(winner.gun != 0 && winner.gun < gunMap[nx][ny].peek()) {
						int winnerGun = winner.gun;
						winner.gun = gunMap[nx][ny].poll();
						gunMap[nx][ny].add(winnerGun);
					}
				}
				//위치정보 갱신
				winner.x = nx;
				winner.y = ny;
				pMap[nx][ny] = winner.index;
			}	
			
//			System.out.println(i+"번 플레이어 이동");
//			printMap();
		}
		
		/*
		 * 플레이어 순서대로 이동
		 * 	1. 진행방향으로 한칸 이동 -> 맵을 벗어나는 경우 정반대 방향으로 1칸 이동한다.
		 * 		1-1. 본인 총과, map[][]의 총을 비교하여 큰 것을 챙김
		 * 		1-2. 플레이어인 경우, 대결
		 * 			play+gun이 큰 순서, player가 큰 사람이 이김 , (차이만큼 승자가 점수를 챙김 score)
		 * 			a. 진 사람은 총이 존재하면, 해당 자리에 내려놓음
		 * 				진행 방향으로 한칸 이동함. -> 다른 사람이 있는 경우, 격자 밖인 경우, while오른쪽으로 90도씩 회전하며 빈칸이 존재하며 이동함.
		 * 				총들이 존재하면 가장 큰 총을 획득함.
		 * 			b. 이긴 사람은 현 위치에 있는 총들 중 가장 데미지가 센총을 획득하고, 다른 총들은 내려놓는다.
		 * */
		
		
	}
	private static void init() throws IOException {
		BufferedReader br = new BufferedReader( new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer(br.readLine());
		N = Integer.parseInt(st.nextToken());
		M = Integer.parseInt(st.nextToken());
		K = Integer.parseInt(st.nextToken());
		
		scores = new int[M + 1];//점수 index는 1부터 시작
		gunMap = new PriorityQueue[N][N]; //총들
		for(int i = 0; i < N; i++) {
			for(int j = 0;  j< N; j++) {
			gunMap[i][j] = new PriorityQueue<>(Collections.reverseOrder());
			}
		}
		
		pMap = new int[N][N]; //플레이어의 인덱스를 기록하는 맵
		players = new Player[M + 1];//플레이어들에 대한 정보를 저장함.
		//		public Player(int index, int x, int y, int dir, int s, int gun) {

		for(int i = 0; i < N; i++) {
			st = new StringTokenizer(br.readLine());
			for(int j = 0; j < N; j++) {
				int gun = Integer.parseInt(st.nextToken());
				if(gun == 0) continue;
				gunMap[i][j].add(gun);
			}
		}
		
		//유저에 대한 정보 -> p map, players 갱신필요
		for(int i = 1; i<=M; i++) {
			st = new StringTokenizer(br.readLine());
			int x = Integer.parseInt(st.nextToken()) - 1;
			int y = Integer.parseInt(st.nextToken()) - 1;
			int d = Integer.parseInt(st.nextToken());
			int s = Integer.parseInt(st.nextToken());
			
			players[i] = new Player(i,x,y,d,s,0);
			pMap[x][y] = i;
		}
		
		
		//TODO 입력 테스트
//		System.out.println("입력입니다.");
//		printMap();
	}
	private static void printMap() {
		System.out.println("	gunMap");
		for(int i = 0; i < N; i++) {
			for(int j = 0; j < N; j++) {
				System.out.print(gunMap[i][j]+"\t");
			}
			System.out.println();
		}
		
		System.out.println("	pMap");
		for(int i = 0; i < N; i++) {
			for(int j = 0; j < N; j++) {
				System.out.print(pMap[i][j]+"\t");
			}
			System.out.println();
		}
		
		System.out.println(" players");
		for(int i = 1; i <= M; i++) {
			System.out.println(players[i]);
		}
		
		System.out.println(Arrays.toString(scores));

		
	}
}