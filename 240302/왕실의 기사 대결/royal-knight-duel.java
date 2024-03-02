import java.io.*;
import java.util.*;

public class Main {

	static class Info {
		int r, c, h, w;

		public Info(int r, int c, int h, int w) {
			this.r = r;
			this.c = c;
			this.h = h;
			this.w = w;
		}

		@Override
		public String toString() {
			return "Info [r=" + r + ", c=" + c + ", h=" + h + ", w=" + w + "]";
		}

	}

	static class Point {
		int r, c, index;

		public Point(int r, int c, int index) {
			this.r = r;
			this.c = c;
			this.index = index;
		}

		@Override
		public String toString() {
			return "r=" + r + " c=" + c + " index=" + index;
		}
	}

	static int[] lifes; // 기사의 체력
	static int[] originLifes; // 초기 기사의 체력
	static int[][] trap; // 지로와 벽의 위치 기록
	static int[][] map; // 기사의 위치 기록
	static int L, N, Q;
	static int[] dx = { -1, 0, 1, 0 };
	static int[] dy = { 0, 1, 0, -1 };

	static Info[] infos;

	public static void main(String[] args) throws IOException {

		/*
		 * 빈칸,함정,벽 이동하려는 방향의 끝에 벽이 있으면 모든 기사는 이동할수없다. 모두 밀린 후에 대미지를 받는다. 밀린 기사는 데미지를 입지
		 * 않는다. 체스판에서 사라진 기사는 반응이없다?
		 */

		// 기사별 좌표를 기록해야함. -> Info class 생성후 폭과 너비만 기록함? 좌측상단의정보와 폭 너비를 기록함.
		// 기사별 체력을 기록해야함. -> int[N] life
		// 지뢰의 위치만 기록하는 int[L][L] trap필요
		// 기사의 위치를 기록하는 int[L][L] map필요

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer(br.readLine());
		L = Integer.parseInt(st.nextToken());
		N = Integer.parseInt(st.nextToken());
		Q = Integer.parseInt(st.nextToken());

		lifes = new int[N + 1];
		originLifes = new int[N + 1];
		trap = new int[L + 1][L + 1];
		map = new int[L + 1][L + 1];
		infos = new Info[N + 1];

		for (int i = 1; i <= L; i++) {
			st = new StringTokenizer(br.readLine());
			for (int j = 1; j <= L; j++) {
				trap[i][j] = Integer.parseInt(st.nextToken());
			}
		}
		// TODO 삭제
//		print(trap);

		for (int i = 1; i <= N; i++) {
			st = new StringTokenizer(br.readLine());
			int r = Integer.parseInt(st.nextToken());
			int c = Integer.parseInt(st.nextToken());
			int h = Integer.parseInt(st.nextToken());
			int w = Integer.parseInt(st.nextToken());
			int k = Integer.parseInt(st.nextToken());
			infos[i] = new Info(r, c, h, w);
			originLifes[i] = k;

			// map에 반영
			for (int dr = 0; dr <= h - 1; dr++) {
				for (int dx = 0; dx <= w - 1; dx++) {
					map[r + dr][c + dx] = i;
				}
			}
		}

		lifes = originLifes.clone();
//		System.out.println(Arrays.toString(lifes));

		// 기사의 이동시 시작됌
		for (int i = 0; i < Q; i++) {
			st = new StringTokenizer(br.readLine());
			int index = Integer.parseInt(st.nextToken());
			int dir = Integer.parseInt(st.nextToken());
			move(index, dir);
//			System.out.println("map");
//			print(map);

		}
		int ans = 0;
		for (int i = 1; i <= N; i++) {
			if (lifes[i] > 0) {
				ans += originLifes[i] - lifes[i];
			}
		}

		System.out.println(ans);
		// 기사 이동 -> 점수 계산
		// int[N] damage배열 초기화
		// int[L][L] tempMap 필요, 이동을 반영해준다.
		// boolean[L][L] bfs를 진행할때 사용
		// 기사의 좌측 상단의 좌표를 기준으로 , h, w범위에 있는 모든 블럭을 이동방향으로 옮김.
		// -이동시에, 도달하는 곳에 지뢰가 있는 경우 damage[기사번호]++
		// -만약 도달하려는 칸에 다른 블록이 있다면, 그 블록도 해당방향으로 민다.
		// -벽(밖으로 나간경우) 이동을 반영하지 않는다.

		// bfs 정상 종료된 경우, 데미지를 반영한다. 만약 기사의 체력이 0이 되면 경기에서 제외시킨다.
		// damages를 lifes에 반영한다.
		// -> bfs시 life가 0이 되면 map에서 삭제함.

		// tempMap에서 map으로 반영함. 이때 삭제된 기사의 경우 0으로 처리함.

	}

	private static void move(int index, int dir) {
//		System.out.println("================================================");
//		System.out.println("startIndex =" + index + " startDir=" + dir);
//		System.out.println(Arrays.toString(infos));
		int[] damages = new int[N + 1];
		int[][] tempMap = new int[L + 1][L + 1];
		Info[] tempInfos = infos.clone();
		for (int i = 1; i <= L; i++) {
			tempMap[i] = map[i].clone();
		}
		boolean[] visited = new boolean[N + 1];

		// 대상 기사가 이동할 수 있는지 확인 lifes를 통해서
		if (lifes[index] <= 0)
			return;
		Queue<Point> q = new LinkedList<>();

		// 대상 기사가 포함된 블럭을 모두 Queue에 넣음.
		// visited[] = true로 변경
		// 이동한 기사의 좌표를 바꿈
		// 이동하는 방향에 따라 첫번째 위치에 있는 값들을 0으로 만듬.
		visited[index] = true;
		Info target = infos[index];
		tempInfos[index] = new Info(target.r + dx[dir], target.c + dy[dir], target.h, target.w);
		for (int i = 0; i <= target.h - 1; i++) {
			for (int j = 0; j <= target.w - 1; j++) {
				q.add(new Point(target.r + i + dx[dir], target.c + j + dy[dir], index));
			}
		}
		// 인덱스 기록
//		System.out.println("begin");
//		print(tempMap);
		for (int i = 0; i <= target.h - 1; i++) {
			for (int j = 0; j <= target.w - 1; j++) {
				tempMap[target.r + i][target.c + j] = 0;
//				System.out.println((target.r + i) + " " + (target.c + j));
			}
		}

//		System.out.println("시작 기사 0만들기");
//		print(tempMap);

		while (!q.isEmpty()) {
//			System.out.println(q);
			Point now = q.poll();
			// 해당자리가 1<=자리<=N인지 파악함.
			if (now.r < 1 || now.c < 1 || now.r > L || now.c > L) {
//				System.out.println("구간을 벗어나 종료");
				return;
			}

			// TODO 추가

			// trap에서 0인 경우 temp에 기사의 index만 반영하고 종료
			// 1인 경우 해당 기사의 damages++;후 temp에 기사 index반영 -> index가 동일한 경우 damege반영 x
			// 2인 경우 move메서드 종료
			if (trap[now.r][now.c] == 2) {
//				System.out.println("벽을 만나 종료");
//				System.out.println(now.index);
				return;
			} else if (trap[now.r][now.c] == 0) {
				tempMap[now.r][now.c] = now.index;
			} else {
				if (now.index != index) {
					damages[now.index]++;
				}
				tempMap[now.r][now.c] = now.index;
			}

			// map에서 0이아닌 값이 존재하는 경우 해당하는 기사의 모든 r,c를 queue에 넣고 visited[]처리한다.
			if (map[now.r][now.c] != 0 && !visited[map[now.r][now.c]]) {
				visited[map[now.r][now.c]] = true;
				target = infos[map[now.r][now.c]];
				tempInfos[map[now.r][now.c]] = new Info(target.r + dx[dir], target.c + dy[dir], target.h, target.w);
//				System.out.println(map[now.r][now.c]);
//				System.out.println(Arrays.toString(infos));
//				System.out.println(Arrays.toString(tempInfos));
				for (int i = 0; i <= target.h - 1; i++) {
					for (int j = 0; j <= target.w - 1; j++) {

						q.add(new Point(target.r + i + dx[dir], target.c + j + dy[dir], map[now.r][now.c]));
						// 이동한 자리를 0으로 만들어 주기 위한 코드
						if (tempMap[target.r + i][target.c + j] == map[now.r][now.c]) {
							tempMap[target.r + i][target.c + j] = 0;
						}
					}
				}
//				System.out.println("다른 기사를 마주한 경우");
//				print(tempMap);
			}
		}

//		System.out.println("결과");
		// 위의 while문을 탈출한경우 -> 문제가 없었으므로, map과 life를 갱신한다.
		Set<Integer> dead = new HashSet<>();
		for (int i = 1; i <= N; i++) {
			lifes[i] -= damages[i];
			if (lifes[i] <= 0) {
				dead.add(i);
			}
		}
		// dead에 포함된 기사의 경우 tempMap -> map에 반영하는 과정에서 0으로 처리한다.
//		System.out.println("dead=" + dead);
		for (int i = 1; i <= L; i++) {
			for (int j = 1; j <= L; j++) {
				map[i][j] = tempMap[i][j];
				if (dead.contains(map[i][j])) {
					map[i][j] = 0;
				}
			}
		}

		infos = tempInfos;

//		print(tempMap);

	}

	private static void print(int[][] map) {
		for (int i = 1; i <= L; i++) {
			for (int j = 1; j <= L; j++) {
				System.out.print(map[i][j] + " ");
			}
			System.out.println();
		}
		System.out.println();
	}

}