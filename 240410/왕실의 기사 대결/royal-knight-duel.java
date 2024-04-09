import java.io.*;
import java.util.*;


public class Main {
	/*
	 * 아래의 과정을 Q번 반복한다.
	 * Q에서 이미 탈락한 기사에게 명령을 내리는 경우를 고려한다.
	 * 
	 * 1.기사 이동(명령 받은 방향으로 한칸만 이동할 수 있다.)
	 * - 시작 기사의 좌표를 먼저 q에 넣는다.
	 * 
	 * bfs를 진행한다.(q가 빌때까지)
	 * -before
	 *  기존 기사의 위치를 기록한 맵을 복사한다. => 이동할 수 있는 경우에 기존 위치의 주소와 교체한다.
	 *	데미지를 기록할 배열을 생성한다. => 이동할 수 있는 경우 damages배열에 반영한다.
	 *
	 *
	 * tempkmap에 좌표 변경을 까먹지 않는다.
	 * -#이동하려는 좌표에서 다른 기사를 만난 경우
	 * ---해당 자리에 있는 기사 index와 좌표를 q에 넣는다.
	 * 
	 * -#다음 좌표에 장애물을 만난 경우
	 * ---데미지를 저장한다. 만약 시작한 기사인 경우는 데미지x
	 * -#벽 만난 경우 => bfs를 종료한다. => 이동 불가
	 * -#경기장 외부로 나갈 경우 => bfs를 종료한다. => 이동 불가
	 * 
	 * 2. 데미지 반영
	 * 임시 데미지 배열에서 damgaes배열에 값을 반영한다.
	 * 만약 기사가 죽는 경우(k가 0이되는 경우) map에서 해당 인덱스를 전부 제거한다.
	 * */
	static int L,N,Q;// L은 체스판의 크기, N은 초기 기사들의 정보, Q명령어의 수
	static int[][] map;//벽,빈칸,함정등 구조를 저장한다.
	static int[][] posMap;//맵에 위치한 기사으의 정보를 반영한다.
	static int[] damages;//각 기사별로 받은 데미지를 저장
	static int[] lifes;//기사들의 초기 목숨을 저장한다.
	static int[][] qs;
	static int[] dx = {-1,0,1,0};
	static int[] dy = {0,1,0,-1};
	static class Info{
		int index;
		int x;
		int y;

		public Info(int index, int x, int y) {
			this.index = index;
			this.x = x;
			this.y = y;
		}

		@Override
		public String toString() {
			return "Info [index=" + index + ", x=" + x + ", y=" + y + "]";
		}
		
		
	}
	public static void main(String[] args) throws IOException {
		init();
		solve();
	}

	private static void solve() {
//		System.out.println("입력");
//		printMap();
		//Q번 반복한다.
		for(int i = 0; i < Q; i++) {
			int index = qs[i][0];
			int dir = qs[i][1];
			
			if(lifes[index] <= 0) continue;
			//1.이동
			move(index, dir);
//			System.out.println();
//			System.out.println("round="+(i+1));
//			System.out.println("index ="+index+" dir="+dir);
//			printMap();
		}
		
		int sum =  0;
		for(int i = 1; i <= N; i++) {
			if(lifes[i] <= 0) continue;
			sum += damages[i];
		}
		
		System.out.println(sum);
		
	}

	private static void move(int index, int dir) {
		//TODO
		//tempPosMap 이동이 가능한 경우 원본에 반영한다.
		int[][] tMap = new int[L][L];
		for(int i = 0; i<L; i++) {
			tMap[i] = posMap[i].clone();
		}
		
		int[] tdam = new int[N + 1];// 임시 데미지 저장 배열, 이동이 가능한 경우 원본 배열에 반영한다.
	
		
		Queue<Info> q = new LinkedList<>();
		
		//명령을 받은 기사의 영역을 먼저 q에 넣는다.
		for(int i = 0; i < L; i++) {
			for(int j = 0; j < L; j++) {
				if(tMap[i][j] == index) {
					int nx = i + dx[dir];
					int ny = j + dy[dir];
					//움직일 수 없는 경우, 벽 or 구간 밖
					if(!isOnRange(nx,ny) || map[nx][ny] == 2) {
						return;
					}
					q.add(new Info(index, nx, ny));
					tMap[i][j] = 0;
				}
			}
		}
		
		
		Set<Integer> visit = new HashSet<>();
		visit.add(index);
		
		while(!q.isEmpty()) {
			Info now = q.poll();
			int nIndex = tMap[now.x][now.y];
			
			if(nIndex != 0 && !visit.contains(nIndex)) {
				visit.add(nIndex);
				
				for(int i = 0; i < L; i++) {
					for(int j = 0; j < L; j++) {
						if(tMap[i][j] == nIndex) {
								tMap[i][j] = 0;
							//다음으로 이동한 구간 - 장애물 // 벽이나 맵을 벗어난 경우
							int nx = i + dx[dir];
							int ny = j + dy[dir];
							
							//이동할 수없는 경우, 맵을 벗어나거나 벽
							if(!isOnRange(nx,ny) || map[nx][ny] == 2) {
								return;
							}

							//장애물을 만난 경우
							if(map[nx][ny] == 1) {
								tdam[nIndex]++;
							}
							q.add(new Info(nIndex, nx, ny));
						}
					}
				}
			}
			
			//이동 좌표 반영
			tMap[now.x][now.y] = now.index;
		
		}
		
		
		//데미지 반영 
		Set<Integer> remove = new HashSet<>();
		for(int i = 1; i <= N; i++) {
			damages[i] += tdam[i];
			lifes[i] -= tdam[i];
			if(lifes[i] <= 0) {
				remove.add(i);
			}
		}
		
		
		for(int i = 0; i < L; i++) {
			for(int j = 0; j < L; j++) {
				if(remove.contains(tMap[i][j])) {
					tMap[i][j] = 0;
				}
			}
		}
		
		//posMap에 반영
		for(int i = 0; i<L; i++) {
			posMap[i] = tMap[i].clone();
		}
		
		/* 
		 * 1.기사 이동(명령 받은 방향으로 한칸만 이동할 수 있다.)
		 * bfs를 진행한다.(q가 빌때까지)
		 * -before
		 *  기존 기사의 위치를 기록한 맵을 복사한다. => 이동할 수 있는 경우에 기존 위치의 주소와 교체한다.
		 *	데미지를 기록할 배열을 생성한다. => 이동할 수 있는 경우 damages배열에 반영한다.
		 *
		 *
		 * tempkmap에 좌표 변경을 까먹지 않는다.
		 * -#이동하려는 좌표에서 다른 기사를 만난 경우
		 * ---해당 자리에 있는 기사 index와 좌표를 q에 넣는다.
		 * 
		 * -#다음 좌표에 장애물을 만난 경우
		 * ---데미지를 저장한다. 만약 시작한 기사인 경우는 데미지x
		 * -#벽 만난 경우 => bfs를 종료한다. => 이동 불가
		 * -#경기장 외부로 나갈 경우 => bfs를 종료한다. => 이동 불가
		 * 
		 * 2. 데미지 반영
		 * 임시 데미지 배열에서 damgaes배열에 값을 반영한다.
		 * 만약 기사가 죽는 경우(k가 0이되는 경우) map에서 해당 인덱스를 전부 제거한다.
		 * */
		
	}

	private static void printMap(int[][] tMap) {
		System.out.println("임시 맵");
		for(int i = 0; i < L; i++) {
			for(int j = 0; j < L; j++) {
				System.out.print(tMap[i][j]+" ");
			}
			System.out.println();
		}
	}

	private static boolean isOnRange(int x, int y) {
		if(x < 0 ||  y < 0 || x >= L ||  y >= L)
			return false;
		return true;
	}

	private static void printMap() {
//		for(int i = 0; i < L; i++) {
//			for(int j = 0; j < L; j++) {
//				System.out.print(map[i][j]+" ");
//			}
//			System.out.println();
//		}
		
		System.out.println("posMap");
		for(int i = 0; i < L; i++) {
			for(int j = 0; j < L; j++) {
				System.out.print(posMap[i][j]+" ");
			}
			System.out.println();
		}
		
		System.out.println("lifes");
		for(int i = 1 ; i <= N; i++) {
			System.out.print(lifes[i]+" ");
		}
		System.out.println();
		
		System.out.println("damages");
		for(int i = 1 ; i <= N; i++) {
			System.out.print(damages[i]+" ");
		}
		System.out.println();
	}

	private static void init() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer(br.readLine());
		L = Integer.parseInt(st.nextToken());//체스판크기
		N = Integer.parseInt(st.nextToken());//기사수
		Q = Integer.parseInt(st.nextToken());//명령어수
		
		//map초기화 0빈칸, 1함정, 2 벽
		map = new int[L][L];
		for(int i = 0; i < L; i++) {
			st = new StringTokenizer(br.readLine());
			for(int j = 0; j < L; j++) {
				map[i][j] = Integer.parseInt(st.nextToken());
			}
		}
		
		//N만큼 기사를 입력받으면서 posMap에 인덱스를 기록함
		posMap = new int[L][L];
		damages = new int[N + 1];
		lifes = new int[N + 1];
		
		for(int i = 1; i<= N; i++) {
			st = new StringTokenizer(br.readLine());
			int r = Integer.parseInt(st.nextToken()) - 1;//좌측상단 x
			int c = Integer.parseInt(st.nextToken()) - 1;//좌측상단y
			int h = Integer.parseInt(st.nextToken());//세로길이
			int w = Integer.parseInt(st.nextToken());//가로길이
			int k = Integer.parseInt(st.nextToken());//초기체력
			
			lifes[i] = k;
			//posMap에 반영
			for(int x = r; x < r + h; x++) {
				for(int y = c; y < c + w; y++) {
					posMap[x][y] = i;
				}
			}
		}
		
		//명령어 저장
		qs = new int[Q][2];
		for(int i = 0; i < Q; i++) {
			st = new StringTokenizer(br.readLine());
			int index = Integer.parseInt(st.nextToken());
			int dir = Integer.parseInt(st.nextToken());
			qs[i][0] = index;//기사 인덱스
			qs[i][1] = dir;//기사가 움직일 방향
			
		}
		
	}

}