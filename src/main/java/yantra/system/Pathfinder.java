package yantra.system;

import java.util.*;
import yantra.util.Vec2;

/** หาเส้นทางสั้นที่สุดจากจุดเกิดไปยังพระอุโบสถด้วย BFS — คำนวณครั้งเดียวตอนโหลดแผนที่ */
public class Pathfinder {

    private static final int[] DX = {1, -1, 0, 0};
    private static final int[] DY = {0, 0, 1, -1};

    public List<Vec2<Integer>> findPath(GameMap map, Vec2<Integer> start, Vec2<Integer> goal) {
        int w = map.getCols(), h = map.getRows();
        int[] cameFrom = new int[w * h];
        Arrays.fill(cameFrom, -1);
        boolean[] visited = new boolean[w * h];
        Deque<Integer> queue = new ArrayDeque<>();

        int startId = start.getY() * w + start.getX();
        int goalId  = goal.getY()  * w + goal.getX();
        queue.add(startId);
        visited[startId] = true;

        while (!queue.isEmpty()) {
            int cur = queue.poll();
            if (cur == goalId) break;
            int cx = cur % w, cy = cur / w;
            for (int i = 0; i < 4; i++) {
                int nx = cx + DX[i], ny = cy + DY[i];
                if (nx < 0 || ny < 0 || nx >= w || ny >= h) continue;
                if (!map.isWalkable(nx, ny)) continue;
                int nid = ny * w + nx;
                if (visited[nid]) continue;
                visited[nid] = true;
                cameFrom[nid] = cur;
                queue.add(nid);
            }
        }

        if (!visited[goalId]) return Collections.emptyList();

        LinkedList<Vec2<Integer>> path = new LinkedList<>();
        for (int cur = goalId; cur != -1; cur = cameFrom[cur]) {
            path.addFirst(new Vec2<>(cur % w, cur / w));
        }
        return path;
    }
}
