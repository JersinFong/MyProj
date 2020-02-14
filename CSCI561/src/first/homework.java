package first;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

class Point{
	int z, x, y, cost, estimate;
	int[] values;
	Point prev;
	Point (int z, int x ,int y, int cost){
		this.z = z;
		this.x = x;
		this.y = y;
		this.cost = cost;
		estimate = 0;
		this.values = new int[]{z, x, y};
		prev = null;
	}
	@Override
	public boolean equals(Object obj) {
	    Point p = (Point) obj;
	    return z == p.z && x == p.x && y == p.y;
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(values);
	}
	@Override
	public String toString() {
		return z + " " + x + " " + y + " " + cost;
	}	
}

class Search{
	private final int[][] DIRS = {{1, -1, 1}, {-1, -1, 1},{-1,  1, 1},{ 1, 1, 1},
								  {0,  1, 1}, { 1,  0, 1},{ 0, -1, 1},{-1, 0, 1}};
	private final int[][] DIRS_WEIGHT = {{1, -1, 14}, {-1, -1, 14},{-1,  1, 14},{ 1, 1, 14},
										{0,  1, 10}, { 1,  0, 10},{ 0, -1, 10},{-1, 0, 10}};
	private int N, M;
	private Map<Point, Set<Integer>> channels;
	
	public Search(int N, int M, int[][]channels){
		this.N = N;
		this.M = M;
		this.channels = new HashMap<>();
		for(int [] channel : channels){
			Point a = new Point(channel[0], channel[1], channel[2], 0);
			Point b = new Point(channel[3], channel[1], channel[2], 0);
			this.channels.putIfAbsent(a, new HashSet<>());
			this.channels.putIfAbsent(b, new HashSet<>());
			this.channels.get(a).add(channel[3]);
			this.channels.get(b).add(channel[0]);
		}
	}
	
	public int bfs(Point s, Point e, Deque<Point> res, int model){
		Queue<Point> q = model == 0 ? new LinkedList<>(): new PriorityQueue<>((a, b) -> a.cost - b.cost);
		int [][] dirs = model == 0 ? DIRS : DIRS_WEIGHT;
		int costs = 0;
		Set<Point> visited = new HashSet<>();
		q.offer(s);
		visited.add(s);
		while(!q.isEmpty()){
				Point cur = q.poll();
				if(cur.equals(e)){
					costs = cur.cost;
					while(cur != null){
						cur.cost = cur.prev == null ? 0 : cur.cost - cur.prev.cost;
						res.addFirst(cur);
						cur = cur.prev;
					}
					return costs;
				}
				int x = cur.x, y = cur.y, z = cur.z, cost = cur.cost;
				for(int [] dir : dirs){
					int nextX = x + dir[0], nextY = y + dir[1];
					Point nextP = new Point(z, nextX, nextY, cost + dir[2]);
					if(nextX >= 0 && nextX < N && nextY >= 0 && nextY < M && !visited.contains(nextP)){
						nextP.prev = cur;
						q.offer(nextP);
						visited.add(nextP);
					}
				}
				Set<Integer> chs = channels.get(cur);
				if(chs != null){
					for(int nextZ: chs){
						int nextCost = model == 0 ? cost + 1 : cost + Math.abs(nextZ - z);
						Point nextP = new Point(nextZ, x, y, nextCost);
						if(!visited.contains(nextP)){
							nextP.prev = cur;
							q.offer(nextP);
							visited.add(nextP);
						}
					}
				}
		}
		return costs;
	}
	
	public int A(Point s, Point e, Deque<Point> res){
		PriorityQueue<Point> q = new PriorityQueue<>((a, b) -> a.estimate - b.estimate);
		int costs = 0, endX = e.x, endY = e.y, endZ = e.z;
		Set<Point> visited = new HashSet<>();
		q.offer(s);
		visited.add(s);
		while(!q.isEmpty()){
				Point cur = q.poll();
				if(cur.equals(e)){
					
					costs = cur.cost;
					while(cur != null){
						cur.cost = cur.prev == null ? 0 : cur.cost - cur.prev.cost;
						res.addFirst(cur);
						cur = cur.prev;
					}
					return costs;
				}
				int x = cur.x, y = cur.y, z = cur.z, cost = cur.cost;
				for(int [] dir : DIRS_WEIGHT){
					int nextX = x + dir[0], nextY = y + dir[1], nextC = cost + dir[2]; 
					Point nextP = new Point(z, nextX, nextY, nextC);
					nextP.estimate = nextC + heuristics(nextX - endX, nextY - endY, z - endZ);
					if(nextX >= 0 && nextX < N && nextY >= 0 && nextY < M && !visited.contains(nextP)){
						nextP.prev = cur;
						q.offer(nextP);
						visited.add(nextP);
					}
				}
				Set<Integer> chs = channels.get(cur);
				if(chs != null){
					for(int nextZ: chs){
						int nextC = cost + Math.abs(nextZ - z);
						Point nextP = new Point(nextZ, x, y, nextC);
						nextP.estimate = nextC + heuristics(x - endX, y - endY, nextZ - endZ);
						if(!visited.contains(nextP)){
							nextP.prev = cur;
							q.offer(nextP);
							visited.add(nextP);
						}
					}
				}
		}
		return costs;
	}
	
	public int heuristics(int diffX, int diffY, int diffZ){
		return Math.abs(diffZ) + (int)Math.sqrt(diffX* diffX + diffY * diffY);
	}
}

public class homework {
	public static void main(String[] args) throws IOException  {
		FileInputStream fstream = new FileInputStream("input.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		String strLine;
		List<String> inputStr = new ArrayList<>();
		while ((strLine = br.readLine()) != null)   {
		  inputStr.add(strLine);
		}
		fstream.close();
		search_choice(inputStr);
	}
	
	private static void search_choice(List<String> inputStr) throws FileNotFoundException, UnsupportedEncodingException{
		String search_name = inputStr.get(0);
		String[] m_n = inputStr.get(1).split(" ");
		int N = Integer.parseInt(m_n[0]);
		int M = Integer.parseInt(m_n[1]);
		String [] point = inputStr.get(2).split(" ");
		Point start = new Point(Integer.parseInt(point[0]), Integer.parseInt(point[1]), Integer.parseInt(point[2]), 0);
		point = inputStr.get(3).split(" ");
		Point end = new Point(Integer.parseInt(point[0]), Integer.parseInt(point[1]), Integer.parseInt(point[2]), 0);
		int len = Integer.parseInt(inputStr.get(4));
		int[][]channels = new int[len][4];
		int j = 0;
		for(int i = 5; i < inputStr.size(); i++){
			String [] channel = inputStr.get(i).split(" ");
			for(int k = 0; k < 4; k++) channels[j][k] = Integer.parseInt(channel[k]);
			j++;
		}
		Search search = new Search(N, M, channels);
		Deque<Point> dq = new ArrayDeque<>();
		int costs = 0;
		if(search_name.equals("BFS")){
			costs = search.bfs(start, end, dq, 0);
		}else if(search_name.equals("UCS")){
			costs = search.bfs(start, end, dq, 1);
		}else {
			costs = search.A(start, end, dq);
		}
		print(costs, dq);
	}
	
	private static void print(int costs, Deque<Point> dq) throws FileNotFoundException, UnsupportedEncodingException{
		PrintWriter writer = new PrintWriter("output.txt", "UTF-8");
		if(dq.size() == 0) writer.println("FAIL");
		else{
			writer.println(costs);
			writer.println(dq.size());
			for(Point p : dq){
				writer.println(p);
			}
		}
		writer.close();
	}
}


