import java.io.*;
import java.util.*;

/*
 * #점수계산메서드
 * 
 * 1.map을 기준으로 bfs를 진행한다.
 * visit배열을 통해서 방문하지 않은 좌표에서 bfs시작
 * - gmap에 인덱스를 기록해야한다.(groups에서 사용할 index)
 * - 시작 좌표를 기록한다.x,y
 * - value를 기록한다.
 * - 같은 그룹의 속한 개수를 파악한다.
 * - groups 리스트에 추가한다.
 * 2.2중 for문을 통해 모든 그룹사이의 관계 계산한다.
 * -bfs를 통해 접해잇는 구간의 개수를 파악한다.
 * -groups의 index를 사용해서 식별한다.!
 * -> (a그룹의 구성원 개수 + b그룹의 구성원 개수)*a그룹벨류*b그룹벨류*접해있는 개수
 * 
 * #회전 메서드
 * 1. +모양 이동, 시작지점의 값들을 저장함. 
 * 2. 각 4각형 회전 map복사형태
 * 
 * */

public class Main {
	static int N;
	static int[][] map;
	static int[][] pMap; //점수계산할때마다갱신 해야함.
	static boolean[][] visited;//점수계산할때마다 갱신
	static int score;
	static List<Group> groups; //점수 계산할때마다 갱신해야함.
	static class Group{
		int id;
		int cnt;
		int x;
		int y;
		
		public Group(int id, int cnt, int x, int y) {
			this.id = id;
			this.cnt = cnt;
			this.x = x;
			this.y = y;
		}

		@Override
		public String toString() {
			return "Group [id=" + id + ", cnt=" + cnt + ", x=" + x + ", y=" + y + "]";
		}
	}
	public static void main(String[] args) throws NumberFormatException, IOException {
		init();
		solve();
	}

	private static void solve() {
		//1.초기 점수 계산
		calScore();
		for(int i = 1; i <= 3; i++) {
			//회전
			rotate();
			//점수계산
			calScore();
		}
		System.out.println(score);
	}

	private static void rotate() {
		int mid = N/2;
		int[][] temp = new int[N][N];
		/*십자가 모양 채우기*/
		//상단
		for(int i = 0; i < mid; i++) {
			temp[i][mid] = map[mid][N-i-1];
		}
		//우측
		for(int col = mid + 1; col < N; col++) {
			temp[mid][col] = map[col][N - mid-1];
		}
		//하단
		for(int row = mid + 1; row < N; row++) {
			temp[row][mid] = map[mid][N - row - 1];
		}
		//좌측
		for(int col = 0; col < mid; col++) {
			temp[mid][col] = map[col][N- mid -1];
		}
		//가운데
		temp[mid][mid] = map[mid][mid];
		
		/*4개의 사각형 90도 회전*/
		//좌측상단
		rotate(0,0, mid, temp);	
		//우측상당
		rotate(0,mid+1,mid,temp);
		//좌측하단
		rotate(mid+1,0,mid,temp);
		//우측하단
		rotate(mid + 1 , mid+1,mid,temp);
		map = temp;
	}

	private static void rotate(int x, int y, int mid, int[][] temp) {
		int[][] rm = new int[mid][mid];//
		for(int i = 0; i < mid; i++) {
			for(int j = 0; j < mid; j++) {
				rm[i][j] = map[x + i][y + j];
			}
		}
		
		int[][] rt = new int[mid][mid];
		for(int i = 0; i < mid; i++) {
			for(int j = 0; j < mid; j++) {
				rt[i][j] = rm[mid - j - 1][i];
			}
		}
		
		for(int i = 0; i < mid; i++) {
			for(int j = 0; j < mid; j++) {
				temp[i + x][j + y] = rt[i][j];
			}
		}
		
		
	}

	private static void printTempMap(int[][] temp) {
		for(int i = 0;  i< N; i++) {
			for(int j = 0; j< N; j++) {
				System.out.print(temp[i][j]+" ");
			}
			System.out.println();
		}
		System.out.println();
		
	
		
		
	}

	private static void printMap() {
		System.out.println("pMap");
		for(int i = 0;  i< N; i++) {
			for(int j = 0; j< N; j++) {
				System.out.print(pMap[i][j]+" ");
			}
			System.out.println();
		}
		
		
		System.out.println("map");
		for(int i = 0;  i< N; i++) {
			for(int j = 0; j< N; j++) {
				System.out.print(map[i][j]+" ");
			}
			System.out.println();
		}
		
		
		System.out.println("score ="+score);
		
	}

	private static void calScore() {
		visited = new boolean[N][N];
		int index = 0;
		
		pMap = new int[N][N];
		groups = new ArrayList<>();
		
		//1.그룹 분리
		for(int i = 0; i < N; i++) {
			for(int j = 0; j< N; j++) {
				if(visited[i][j]) continue;
				int cnt = grouping(index, i,j);
				groups.add(new Group(index++,cnt,i,j));
			}
		}
		//2.점수 계산
		for(int i = 0; i < groups.size(); i++) {
			for(int j = i + 1; j < groups.size(); j++) {
				Group a = groups.get(i);
				Group b = groups.get(j);
			

				//인접한 면 찾기
				int contactingCnt = countTouching(a,b);
				int tScore = (a.cnt + b.cnt) * map[a.x][a.y] * map[b.x][b.y] * contactingCnt;
				score += tScore;
			}
		}
		
		 /* 1.map을 기준으로 bfs를 진행한다.
		 * visit배열을 통해서 방문하지 않은 좌표에서 bfs시작
		 * - gmap에 인덱스를 기록해야한다.(groups에서 사용할 index)
		 * - 시작 좌표를 기록한다.x,y
		 * - value를 기록한다.
		 * - 같은 그룹의 속한 개수를 파악한다.
		 * - groups 리스트에 추가한다.
		 * 2.2중 for문을 통해 모든 그룹사이의 관계 계산한다.
		 * -bfs를 통해 접해잇는 구간의 개수를 파악한다.
		 * -groups의 index를 사용해서 식별한다.!
		 * -> (a그룹의 구성원 개수 + b그룹의 구성원 개수)*a그룹벨류*b그룹벨류*접해있는 개수*/
	}
	
	private static int countTouching(Group a, Group b) {
		boolean cvisited[][] = new boolean[N][N];
		int sx = a.x;
		int sy = a.y;
		int id = a.id;
		cvisited[sx][sy] = true;
		Queue<int[]> q = new LinkedList<>();
		q.add(new int[]{sx,sy});
		
		//다음칸pmap값이 id와 동일한 경우 q에 추가
		//다음칸의 pmap값이 b.id와 동일한 경우 cnt++;
		int cnt = 0;
		int dx[] = {-1,1,0,0};
		int dy[] = {0,0,-1,1};
		while(!q.isEmpty()) {
			int[] now = q.poll();
			int x = now[0];
			int y = now[1];
			for(int i = 0; i < 4; i++) {
				int nx = x + dx[i];
				int ny = y + dy[i];
				
				if(!isOnRange(nx,ny) || cvisited[nx][ny]) {
					continue;
				}
				
				if(pMap[nx][ny] == id) {
					cvisited[nx][ny] = true;
					q.add(new int[] {nx,ny});
				}
				
				if(pMap[nx][ny] == b.id) {
					cnt++;
				}
			}

		}
		
		return cnt;
	}

	//해당 그룹에 몇개인지 숫자를 세며 pMap에 index를 반영한다.
	private static int grouping(int index, int sx, int sy) {
		int value = map[sx][sy];
		Queue<int[]> q = new LinkedList<>();
		visited[sx][sy] = true;
		pMap[sx][sy] = index;
		q.add(new int[] {sx,sy});
		
		int cnt = 0;
		//pMap에 index를 기록한다.
		//다음칸이 동일한 value를 가지고 있으면 q에 넣는다.
		//visited처리를 해준다.
		int dx[] = {-1,1,0,0};
		int dy[] = {0,0,-1,1};
		while(!q.isEmpty()) {
			int[] now = q.poll();
			int x = now[0];
			int y = now[1];
			cnt++;
			for(int i = 0; i < 4; i++) {
				int nx = x + dx[i];
				int ny = y + dy[i];
				if(!isOnRange(nx,ny) || map[nx][ny] != value || visited[nx][ny]) {
					continue;
				}
				pMap[nx][ny] = index;
				visited[nx][ny] = true;
				q.add(new int[] {nx, ny});
			}
		}
		return cnt;
	}

	private static boolean isOnRange(int x, int y) {
		if(x < 0 || y < 0 || x >= N || y >= N) {
			return false;
		}
		return true;
	}

	private static void init() throws NumberFormatException, IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		N = Integer.parseInt(br.readLine());
		map = new int[N][N];
		
		for(int i = 0; i < N; i++) {
			StringTokenizer st = new StringTokenizer(br.readLine());
			for(int j = 0; j < N; j++) {
				map[i][j] = Integer.parseInt(st.nextToken());
			}
		}
		groups = new ArrayList<>();
	}
	
	

}