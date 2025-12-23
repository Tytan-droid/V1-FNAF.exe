package Class.animatronics;

import java.awt.Graphics;
import java.util.Random;

import Class.GamePanel;
import Class.Main;
import Class.SoundManager;
import Class.rooms.Rooms_Graph;
import Class.rooms.abstrac_room;

public abstract class abstrac_animatronic {
    private String id_room;
    private int difficultie;
    private int etape_mvt;
    private int coter;      //0 gauche et 1 droite

    public abstrac_animatronic(String id_room,int difficultie,int etape_mvt){
        this.id_room =id_room;
        this.difficultie=difficultie;
        this.etape_mvt=etape_mvt;
    }

    public String get_id_room(){
        return this.id_room;
    }
    public int get_difficultie(){
        return this.difficultie;
    }    
    public int get_etape_mvt(){
        return this.etape_mvt;
    }
    public void set_etape_mvt(int new_etape_mvt){
        this.etape_mvt=new_etape_mvt;
    }
    public void set_id_room(String id_room){
        this.id_room=id_room;
    }

    public void set_is_here(){

    }

    public int get_etape(){
        return 0;
    }

    public void move(Rooms_Graph rg){
        this.update_coter();
        Random rand = new Random();
        int n = rand.nextInt(20) + 1;
        String id_room = this.get_id_room();
        if (this.get_etape_mvt()==4*60 && n<=this.get_difficultie() ){
            this.set_etape_mvt(0);
            abstrac_room r= rg.getRoom(this.get_id_room());
            int size = rg.getNeighbors(r).size();
            if(size>0){
                n = rand.nextInt(size);
                this.set_id_room(rg.getNeighbors(r).get(n).get_name());
            }
            n=rand.nextInt(100);
            if (n<=50+this.difficultie){
                this.set_id_room(rg.approch_you(r).get_name());
            }
            if(id_room.equals("Door_Right") && !rg.getNeighbors(r).contains(rg.getRoom("You"))){
                this.set_id_room("CAM1B");
            }
            if(id_room.equals("Door_Left") && !rg.getNeighbors(r).contains(rg.getRoom("You"))){
                this.set_id_room("CAM1B");
            }
            this.mvt_sound();
        }else if(this.get_etape_mvt()<4*60){
            this.set_etape_mvt(this.get_etape_mvt()+1);
        }else{
            this.set_etape_mvt(0);
        }
    }

    public void kill(){
        if (this.get_id_room().equals("You")){
            Main.startJumpscare(this);
        }
    }

    public void mvt_sound(){
        if(this.get_id_room().equals("Door_Right")||this.get_id_room().equals("Door_Left")){
            SoundManager.play("Deep_Steps");    
        }
    }

    public void set_coter(int c){
        this.coter=c;
    }
    public int get_coter(){
        return this.coter;
    }

    public void update_coter(){
        if (this.id_room.equals("Door_Left")){
            this.set_coter(0);
        }else if(this.id_room.equals("Door_Right")){
            this.set_coter(1);
        }
    }

    public boolean get_is_here(){
        return true;
    }

    public void rewind(){
    }
    public void end_rewind(){
    }
}

