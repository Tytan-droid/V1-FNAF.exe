package Class.animatronics;

import java.util.Random;

import Class.Main;
import Class.rooms.Rooms_Graph;
import Class.rooms.abstrac_room;

public class Golden_Freddy extends abstrac_animatronic{
    private boolean is_here;
    private boolean cam_proc;
    public Golden_Freddy(String id_room,int difficultie,int etape_mvt){
        super(id_room, difficultie, etape_mvt);
        this.is_here=false;
        this.cam_proc=false;

    }

    public void spawn(){
        Random rand = new Random();
        int n = rand.nextInt(100) + 1;
        if(n<=this.get_difficultie()){
            this.is_here=true;
        }
    }

    public void despawn(){
        this.is_here=false;
        this.set_etape_mvt(0);
    }

    @Override
    public boolean get_is_here(){
        return this.is_here;
    }

    @Override
    public void move(Rooms_Graph rg){
        if (this.is_here && this.get_etape_mvt()==3*60){
            this.is_here=false;
            Main.startJumpscare(this);
        }else if(this.is_here && this.get_etape_mvt()<3*60){
            this.set_etape_mvt(this.get_etape_mvt()+1);
        }else{
            if(Main.isCam()){
                this.cam_proc=true;
            }else if(cam_proc){
                this.spawn();
                cam_proc=false;
            }
        }
        if(this.is_here && Main.isCam()){
            this.despawn();
        }
    }

    @Override
    public void kill(){
    }
    @Override
    public void set_is_here(){
        this.is_here=false;
    }
}
