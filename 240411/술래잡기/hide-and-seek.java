import java.io.*;
import java.util.*;

public class Main {
    /*

 * 
 * k번 반복하며 술래의 점수를 계산한다.
 * 1.도망자 이동
 * for을 통해 map을 순환한다.
 * -사람이 존재하는경우
 * --#술래와의 거리가 3이상인 사람만 움직인다.
 * --dir[index]를 통해 해당 방향으로 이동시킨다.
 * --#격자를 벗어나지 않는경우
 * --- 한칸 앞에 술래가 존재하면 움직이지 않는다.
 * --#격자를 벗어나는 경우
 * --- 방향을 반대방향으로 튼다. 
 * --- 해당 방향 한칸 앞에 술래가 존재하지 않으면 1칸 이동한다.
 * 
 * 2.술래이동
 * ??술래가 방향전환하는 순간은 1 *2번, 2*2번, 3 * 2번 회전방향은 오른쪽
 * ??만약 (0,0)에 도착한 순간.방향은 아래를 바라보게한다.이후 회전방향은 왼쪽
 * 
 * 술래는 한턴에 1칸씩 이동한다.
 * 이동 후,방향전환이 필요한 경우 바로 전환한다.
 * 술래의 시선으로 3칸(술래자리~,+1,+2)에 존재하는 도망자들을 잡는다.
 * -칸에 나무가 있으면 찾지 못한다.
 * -나무가 없는 경우 도망자가 존재하는 경우
 *     cnt를 증가시킨다.
 * 
 * #술래에게 잡힌 경우 점수계산
 * 해당 round * 잡힌 수(cnt)를 총 점수에 더한다.
 * 
 * */
/*
 * int[][] treeMap; // 나무와 술래의 위치를 기록한다.
 * List<Integer>[][] map// 도망자의 인덱스를 기록함.
 * int[] dirs; // 도망자의 진행 방향을 저장함, 술래의 진행방향은 [0]에 저장할까?
 * Point tagger 술래의 위치
 * */

static int N,M,H,K;
static int round;
static int[][] treeMap;//나무와 술래의 위치를 기록한다.
static Queue<Integer>[][] runnerMap;//도망자의 인덱스르 기록한다.
static int[] dirs;//술래, 도망자의 진행방향을 저장한다.
static Point tagger;//술래의 위치
static int[] dx = {-1,0,1,0};//상, 우, 하, 좌
static int[] dy = {0,1,0,-1};
static int score; //점수
static boolean isRightWay = true; //원점이나 중간지점에 닿는 경우 변경해줘야한다.
static int turnCnt; //turnCnt가 2가 되면 방향을 변경해준다.
static int moveDist = 1;//moveCnt가 limitCnt가 되면 turnCnt를++한다.그리고 moveCnt = 0으로 만든다.
static int moveCnt;//술래가 이동한 횟수.

static class Point{
    int x;
    int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    @Override
    public String toString() {
        return "point [x=" + x + ", y=" + y + "]";
    }
    
    
}
public static void main(String[] args) throws IOException {
    init();
    solve();
}

private static void solve() {

	for(int i = 1; i<= K; i++) {
        round = i; //점수 계산에 사용해야함
        //1.도망자이동
            runnerMove();
            //2.술래이동
            moveTagger();

		}
    System.out.println(score);
}


private static void moveTagger() {
    //전진
    moveCnt++;
    tagger.x += dx[dirs[0]];
    tagger.y += dy[dirs[0]];
    //moveCnt가 limitCnt가 되면
    if(moveCnt == moveDist) {
        moveCnt = 0;
        turnCnt++;
        //방향 전환
        if(isRightWay) {
            dirs[0]++;
            if(dirs[0] == 4) {
                dirs[0] = 0;
            }
        }
        //역방향인 경우
         else {
             dirs[0]--;
             if(dirs[0] == -1) {
                 dirs[0] = 3;
                }
        }
    
    }
    //turnCnt가 2가되면 이동해야할 거리를 증가/감소 시킨다.
    if(turnCnt == 2) {
        turnCnt = 0;
        if(isRightWay) {
            moveDist++;
        }else {
            moveDist--;
        }
        
    }
    
    //TODO 원점이나, 중앙 으로 돌아온경우 moveCnt, taggerMoveDir, isRightWay조작
    if(tagger.x == 0 && tagger.y == 0) {
        dirs[0] = 2;
    
        moveCnt = 0;
        moveDist = N - 1;
        turnCnt = -1;
        isRightWay = false;
    }
    if(tagger.x == N/2 && tagger.y == N/2) {
        dirs[0] = 0;
        moveCnt = 0;
        moveDist = 1;
        turnCnt = 0;
        isRightWay = true;
    }
    
    //TODO
    /*시야의 있는 사람 잡기*/

    int cnt = 0;
    int nx = tagger.x;
    int ny = tagger.y;
    //자기자리 포함해서 총 3칸을 봄
    for(int i = 0; i < 3; i++) {
    	nx += dx[dirs[0]];
    	ny += dy[dirs[0]];
    	
    	if(!isOnRange(nx,ny)) {
    		break;
    	}
    	
    	if(treeMap[nx][ny] == 1)
    		continue;
    	
    	//해당자리에 있는 사람들 숫자 세기
    	if(runnerMap[nx][ny].size() > 0) {
    		cnt += runnerMap[nx][ny].size();
    		runnerMap[nx][ny].clear();
    	}
  
    }
    
  	score += (round * cnt);
    
    /*
     *??술래가 방향전환하는 순간은 1 *2번, 2*2번, 3 * 2번 회전방향은 오른쪽
     * ??만약 (0,0)에 도착한 순간.방향은 아래를 바라보게한다.이후 회전방향은 왼쪽
     * 
     * 술래는 한턴에 1칸씩 이동한다.
     * 이동 후,방향전환이 필요한 경우 바로 전환한다.
     * 술래의 시선으로 3칸(술래자리~,+1,+2)에 존재하는 도망자들을 잡는다.
     * -칸에 나무가 있으면 찾지 못한다.
     * -나무가 없는 경우 도망자가 존재하는 경우
     *     cnt를 증가시킨다.
     * 
     * #술래에게 잡힌 경우 점수계산
     * 해당 round * 잡힌 수(cnt)를 총 점수에 더한다.    
     */    
}

private static void runnerMove() {
    Queue<Integer>[][] nextRunnerMap = new LinkedList[N][N];
    for(int i = 0; i < N; i++) {
        for(int j = 0; j < N; j++) {
            nextRunnerMap[i][j] = new LinkedList<>();
        }
    }
    
    for(int i = 0; i < N; i++) {
        for(int j = 0; j < N; j++) {
            //안에 무언가 존재하는 경우
            while(!runnerMap[i][j].isEmpty()) {
                int runnerIndex = runnerMap[i][j].poll();
                //i,j는 현재 좌표이다.
                int dist = calDist(i, j, tagger.x, tagger.y);
                //술래아의 거리가 3이하인 경우에 이동한다.
                if(dist <= 3) {
                    int dir = dirs[runnerIndex];
                    int nx = i + dx[dir];
                    int ny = j + dy[dir];
                    
                    //구간 내부에 있는 경우
                    if(isOnRange(nx,ny)) {
                        //앞에 술래가 존재하는 경우
                        if(nx == tagger.x && ny == tagger.y) {
                            //제자리에 그대로
                            nextRunnerMap[i][j].add(runnerIndex);
                            continue;
                        }
                        //앞에 술래가 존재하지 않으면 이동한다.
                        nextRunnerMap[nx][ny].add(runnerIndex);
                    
                    //구간을 벗어나는 경우    
                    }else {
                        //반대방향으로 돌린다.
                        dirs[runnerIndex] = dirs[runnerIndex] + 2 % 4;
                        dir = dirs[runnerIndex];
                        //술래가 존재하지 않으면 한칸 이동한다.
                        nx = i + dx[dir];
                        ny = j + dy[dir];
                        //술래가 존재하는 경우
                        if(nx == tagger.x && ny == tagger.y) {
                            nextRunnerMap[i][j].add(runnerIndex);
                            continue;
                        }
                        //술래가 존재하지 않는 경우
                        nextRunnerMap[nx][ny].add(runnerIndex);
                    }
                    
                }
            }
        }
    }
    runnerMap = nextRunnerMap;
    /*
     * 1.도망자 이동
     * for을 통해 map을 순환한다.
     * -사람이 존재하는경우
     * --#술래와의 거리가 3이상인 사람만 움직인다.
     * --dir[index]를 통해 해당 방향으로 이동시킨다.
     * --#격자를 벗어나지 않는경우
     * --- 한칸 앞에 술래가 존재하면 움직이지 않는다.
     * --#격자를 벗어나는 경우
     * --- 방향을 반대방향으로 튼다. 
     * --- 해당 방향 한칸 앞에 술래가 존재하지 않으면 1칸 이동한다.*/
    
}
private static void printTaggerPosition() {
    int[][] map= new int[N][N];
    map[tagger.x][tagger.y] = 1;
    for(int i = 0; i < N; i++) {
        for(int j = 0; j < N; j++) {
            System.out.print(map[i][j]+" ");
        }
        System.out.println();
    }
    
}
private static boolean isOnRange(int x, int y) {
    if(x < 0 || y < 0 || x >= N || y >= N) {
        return false;
    }
    return true;
}

private static int calDist(int x1, int y1, int x2, int y2) {
    return Math.abs(x1-x2) + Math.abs(y1-y2);
}

private static void printMap() {
    System.out.println("treeMap");
    for(int i = 0; i < N; i++) {
        for(int j = 0; j < N; j++) {
            System.out.print(treeMap[i][j]+" ");
        }
        System.out.println();
    }
    
    System.out.println("taggerPosition");
    printTaggerPosition();
    
    System.out.println("runnerMap");
    for(int i = 0; i < N; i++) {
        for(int j = 0; j < N; j++) {
            System.out.print(runnerMap[i][j]+" ");
        }
        System.out.println();
    }
    
    System.out.println("runnerDir");
    for(int i = 1; i <= M; i++) {
        System.out.print("i="+i+" dirs="+dirs[i]+" ");
    }
    System.out.println();
    
    
    System.out.println("taggerInfo");
    System.out.println(tagger + "dirs[0]=" + dirs[0]);
}

private static void init() throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    StringTokenizer st = new StringTokenizer(br.readLine());
    N = Integer.parseInt(st.nextToken()); //격자
    M = Integer.parseInt(st.nextToken()); //도망자수
    H = Integer.parseInt(st.nextToken()); //나무수
    K = Integer.parseInt(st.nextToken()); //진행 라운드 수

    runnerMap = new LinkedList[N][N];
    dirs = new int[N + 1];
    for(int i = 0; i < N; i++) {
        for(int j = 0; j < N; j++) {
            runnerMap[i][j] = new LinkedList<>();
        }
    }
    //i는 도망자의 인덱스로 취급한다.
    for(int i = 1; i<=M; i++) {
        st = new StringTokenizer(br.readLine());
        int x = Integer.parseInt(st.nextToken()) - 1;
        int y = Integer.parseInt(st.nextToken()) - 1;
        int d = Integer.parseInt(st.nextToken()); //움직이는 방향
        
        //상 우 하 좌
        //이동해야할 방향 과 runnerMap에 효기한다.
        dirs[i] = d == 1 ? 1 : 2; //1인경우 좌,우로 움직인다. 2인 경우 상,하로 움직인다.
        runnerMap[x][y].add(i);
    }
    treeMap = new int[N][N];
    for(int i = 0; i < H; i++) {
        st = new StringTokenizer(br.readLine());
        int tx = Integer.parseInt(st.nextToken()) - 1;
        int ty = Integer.parseInt(st.nextToken()) - 1;
        treeMap[tx][ty] = 1; //나무가 존재하는 곳은 1로 표기한다.
    }
    
    //술래의 대한 시작 정보
    tagger = new Point(N/2, N/2);
    dirs[0] = 0; //위를 보며 시작하기 때문
}
}