package Class.rooms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Rooms_Graph {
    private Map<abstrac_room, List<abstrac_room>> adjacencyList;

    public Rooms_Graph() {
        this.adjacencyList = new HashMap<>();
    }

    public void addNode(abstrac_room node) {
        adjacencyList.putIfAbsent(node, new ArrayList<>());
    }

    public void addEdge(abstrac_room node1, abstrac_room node2) {
        adjacencyList.get(node1).add(node2);
        adjacencyList.get(node2).add(node1);
    }

    public void removeNode(abstrac_room node) {
        adjacencyList.values().stream().forEach(e -> e.remove(node));
        adjacencyList.remove(node);
    }

    public void removeEdge(abstrac_room node1, abstrac_room node2) {
        List<abstrac_room> list1 = adjacencyList.get(node1);
        List<abstrac_room> list2 = adjacencyList.get(node2);
        if (list1!= null) {
            list1.remove(node2);
        }
        if (list2!= null) {
            list2.remove(node1);
        }
    }

    public List<abstrac_room> getNeighbors(abstrac_room node) {
        return adjacencyList.get(node);
    }
    
public abstrac_room getRoom(String room_name) {
    for (abstrac_room room : adjacencyList.keySet()) {
        if (room.get_name().equals(room_name)) {
            return room;
        }
    }
    return null;
}


    public Rooms_Graph Rooms_Graph_Builder(){
        Room CAM1A = new Room(0, "CAM1A",5);
        Room CAM1B = new Room(1, "CAM1B",4);
        Room CAM1C = new Room(2, "CAM1C",3);
        Room CAM2A = new Room(3, "CAM2A",2);
        Room CAM2B = new Room(4, "CAM2B",1);
        Room CAM3 = new Room(5, "CAM3",3);
        Room CAM4A = new Room(6, "CAM4A",2);
        Room CAM4B = new Room(7, "CAM4B",1);
        Room CAM5 = new Room(8, "CAM5",4);
        Room CAM6 = new Room(9, "CAM6",4);
        Room CAM7 = new Room(10, "CAM7",4);
        Room Door_Left = new Room(11, "Door_Left",0);
        Room Door_Right = new Room(12, "Door_Right",0);
        Office You = new Office(13, "You");

        this.addNode(CAM1A);
        this.addNode(CAM1B);
        this.addNode(CAM1C);
        this.addNode(CAM2A);
        this.addNode(CAM2B);
        this.addNode(CAM3);
        this.addNode(CAM4A);
        this.addNode(CAM4B);
        this.addNode(CAM5);
        this.addNode(CAM6);
        this.addNode(CAM7);
        this.addNode(Door_Left);
        this.addNode(Door_Right);
        this.addNode(You);

        this.addEdge(CAM1A, CAM1B);
        this.addEdge(CAM1C, CAM1B);
        this.addEdge(CAM5, CAM1B);
        this.addEdge(CAM7, CAM1B);
        this.addEdge(CAM6, CAM1B);
        this.addEdge(CAM4A, CAM1B);
        this.addEdge(CAM2A, CAM1C);
        this.addEdge(CAM2A, CAM2B);
        this.addEdge(CAM4A, CAM4B);
        this.addEdge(CAM2B, Door_Left);
        this.addEdge(CAM4B, Door_Right);
        this.addEdge(You, Door_Left);
        this.addEdge(You, Door_Right);

        return this;
    }

    public abstrac_room approch_you(abstrac_room r){
        abstrac_room rep=r;
        int dist_min = 10;
        for(abstrac_room r1: this.getNeighbors(r)){
            if(r1.get_dist()<dist_min){
                dist_min=r1.get_dist();
                rep=r1;
            }
        }
        return rep;
    }
        public abstrac_room approch_you_left(abstrac_room r){
        abstrac_room rep=r;
        int dist_min = 10;
        for(abstrac_room r1: this.getNeighbors(r)){
            if(r1.get_dist()<dist_min && !r1.get_name().equals("CAM4A")){
                dist_min=r1.get_dist();
                rep=r1;
            }
        }
        return rep;
    }
}