package Class.animatronics;

import java.util.Random;

import Class.Main;
import Class.SoundManager;
import Class.rooms.Rooms_Graph;

public class Foxy extends abstrac_animatronic {
    public Foxy(String id_room,int difficultie,int etape_mvt){
        super(id_room, difficultie, etape_mvt);
    }
    
    @Override
    public void mvt_sound(){
        SoundManager.play("fnaf-running");
    }

    @Override
    public void move(Rooms_Graph rg){
        this.update_coter();
        Random rand = new Random();
        int n = rand.nextInt(20) + 1;
        String id_room = this.get_id_room();
        if (this.get_etape_mvt()==16*60 && n<=this.get_difficultie() ){
            this.mvt_sound();
            this.set_id_room("CAM6");
            id_room = this.get_id_room();
            this.set_etape_mvt(this.get_etape_mvt()+1);
        }
        if(this.get_etape_mvt()<18*60 && this.get_etape_mvt()!=16*60){
            this.set_etape_mvt(this.get_etape_mvt()+1);
        }else if(this.get_etape_mvt()>=18*60 && this.get_id_room().equals("CAM6")){
            this.run(rg);
        }
        if (Main.isCam() && Main.getCurrentCamera().equals(id_room)&& id_room.equals("CAM1C")){
            this.set_etape_mvt(0);
        }
        if(this.get_etape_mvt()==7*60){
            this.set_id_room("CAM1C");
        }else if(this.get_id_room().equals("Door_Left")){
                if(rg.getNeighbors(rg.getRoom("Door_Left")).contains(rg.getRoom("You"))){
                    this.set_id_room("You");
                    this.kill();
            }
        }
    }

    public void run(Rooms_Graph rg){
        if(!rg.getNeighbors(rg.getRoom("Door_Left")).contains(rg.getRoom("You"))){
            this.set_id_room("CAM1C");
            this.set_etape_mvt(0);
            SoundManager.play("Foxy_Pound_Test");
            this.set_id_room("Door_Left");
        }else{
            this.set_id_room("You");
            this.kill();
        }
    }
}
