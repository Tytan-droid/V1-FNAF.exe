package Class.animatronics;

import java.util.Random;

import Class.Main;
import Class.SoundManager;
import Class.rooms.Rooms_Graph;
import Class.rooms.abstrac_room;

public class Freddy_Eyes extends abstrac_animatronic {

    public  Freddy_Eyes(String id_room,int difficultie,int etape_mvt){
        super(id_room,difficultie,etape_mvt);
        this.set_coter(0);
    }

    @Override
    public void kill(){
    }

    @Override
    public void mvt_sound(){
    }

    @Override
    public void move(Rooms_Graph rg){
    }

    @Override
    public void update_coter(){
    }

}
