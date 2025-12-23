package Class.animatronics;

import java.util.List;

import Class.Main;
import Class.SoundManager;
import Class.rooms.Rooms_Graph;

public class Puppet extends abstrac_animatronic{
    int etape;
    private boolean is_here;
    private boolean rewind;
    public Puppet(String id_room, int difficultie, int etape_mvt ){
        super(id_room, difficultie, etape_mvt);
        this.etape=0;
        this.is_here=true;
        this.rewind=false;

        
    }
    @Override
    public boolean get_is_here(){
        return this.is_here;
    }

    @Override
    public void kill(){
    }

    @Override
    public void move(Rooms_Graph rg){
        if(!this.rewind){
            if (this.get_etape_mvt()>=(5-this.get_difficultie()*3/20)*60){
                if(this.etape>=4){
                    this.is_here=false;
                    Main.startJumpscare(this);
                }else if(this.etape==3){
                    SoundManager.play("fnaf-2-music-box");
                    this.etape++;
                    this.set_etape_mvt(0);
                }else{
                    this.etape++;
                    this.set_etape_mvt(0);
                }
            }else if(this.get_etape_mvt()<(5-this.get_difficultie()*3/20)*60){
                this.set_etape_mvt(this.get_etape_mvt()+1);
            }
        }
    }

    @Override
    public int get_coter(){
        return 1;
    }

    @Override
    public void set_is_here(){
        this.is_here=false;
    }

    public void set_rewind(boolean b){
        this.rewind=b;
    }

    public boolean get_rewind(){
        return this.rewind;
    }

    @Override
    public void rewind(){
        this.set_etape_mvt(this.get_etape_mvt()-2);
        if (!get_rewind()){
            this.set_rewind(true);
            SoundManager.stop("fnaf-2-music-box");
            SoundManager.loop("puppet-music-box");
        }
        if(this.get_etape_mvt()<=0){
            this.set_etape_mvt(0);
            if(this.etape>0){
                this.etape--;
                this.set_etape_mvt(4*60);
            }
        }
    }
    @Override
    public void end_rewind(){
        this.set_rewind(false);
        SoundManager.stop("puppet-music-box");
    }

    @Override
        public int get_etape(){
        return this.etape;
    }

}
