package Class.animatronics;

import java.util.Random;

import Class.Main;
import Class.SoundManager;
import Class.rooms.Rooms_Graph;
import Class.rooms.abstrac_room;

public class Freddy extends abstrac_animatronic {

    public  Freddy(String id_room,int difficultie,int etape_mvt){
        super(id_room,difficultie,etape_mvt);
    }

    @Override
    public void kill(){
        if ((this.get_id_room().equals("Door_Left"))||this.get_id_room().equals("Door_Right")) {
            this.set_id_room("You");
            Main.startJumpscare(this);
        }
    }

    @Override
    public void mvt_sound(){
        SoundManager.play("fnaf-freddys-laugh");
    }

    @Override
    public void move(Rooms_Graph rg){
        if(this.get_id_room().equals("You")){
            return;
        }
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
            if (n<=50+this.get_difficultie()){
                this.set_id_room(rg.approch_you(r).get_name());
            }
            if(id_room.equals("CAM4B") && !rg.getNeighbors(rg.getRoom("Door_Right")).contains(rg.getRoom("You"))){
                this.set_id_room("CAM4B");
                return;
            }
            if(id_room.equals("CAM2B") && !rg.getNeighbors(rg.getRoom("Door_Left")).contains(rg.getRoom("You"))){
                this.set_id_room("CAM2B");
                return;
            }
            this.mvt_sound();
        }else if(this.get_etape_mvt()<4*60){
            this.set_etape_mvt(this.get_etape_mvt()+1);
        }else{
            this.set_etape_mvt(0);
        }
        if(Main.isCam() && Main.getCurrentCamera().equals(this.get_id_room())){
            this.set_etape_mvt(0);
        }
    }

    @Override
    public void update_coter(){
        if (this.get_id_room().equals("CAM2A")){
            this.set_coter(0);
        }else if(this.get_id_room().equals("CAM4A")){
            this.set_coter(1);
        }
    }

}
