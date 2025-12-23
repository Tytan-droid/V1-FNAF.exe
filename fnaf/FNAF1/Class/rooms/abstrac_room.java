package Class.rooms;

public abstract class abstrac_room {
    private int id;
    private String name;
    private int dist;
    
    public abstrac_room(int id,String name,int dist) {
        this.id = id;
        this.name=name;
        this.dist=dist;
    }

    public int get_id() {
        return this.id;
    }
    public String get_name(){
        return this.name;
    }
    public int get_dist(){
        return this.dist;
    }
}
