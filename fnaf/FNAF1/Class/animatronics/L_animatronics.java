package Class.animatronics;

import java.util.ArrayList;
import java.util.List;

import Class.rooms.Rooms_Graph;

public class L_animatronics {
    private List<abstrac_animatronic> la;

    public L_animatronics(){
        this.la= new ArrayList<>();
    }

    public List<abstrac_animatronic> get_L(){
        return this.la;
    }

    public void Add_animatronic(abstrac_animatronic animatronic){
        this.la.add(animatronic);
    }

    public void L_animatronics_Builder_n1(){
        this.L_animatronics_Builder(0, 3, 2, 2, 0,0);
    }
    public void L_animatronics_Builder_n2(){
        this.L_animatronics_Builder(0, 6, 3, 3, 0,3);
    }
    public void L_animatronics_Builder_n3(){
        this.L_animatronics_Builder(2, 3, 7, 4, 0,6);
    }
    public void L_animatronics_Builder_n4(){
        this.L_animatronics_Builder(5, 8, 7, 5, 1,9);
    }
    public void L_animatronics_Builder_n5(){
        this.L_animatronics_Builder(7, 11, 11, 7, 3,12);
    }
    public void L_animatronics_Builder_n6(){
        this.L_animatronics_Builder(11, 14, 14, 8, 5,15);
    }
    public void L_animatronics_Builder_custom(int[] dif){
        this.L_animatronics_Builder(dif[0], dif[1], dif[2], dif[3], dif[4],dif[5]);
    }


    public void L_animatronics_Builder(int difficultie_freddy,int difficultie_bonnie,int difficultie_chica,int difficultie_foxy, int difficultie_golden_freddy,int difficultie_puppet){
        Chica c = new Chica("CAM1A", difficultie_chica, 0);
        Bonnie b = new Bonnie("CAM1A", difficultie_bonnie, 0);
        Freddy f = new Freddy("CAM1A",difficultie_freddy,0);
        Foxy f2 = new Foxy("CAM1C", difficultie_foxy, 0);
        Golden_Freddy gf = new Golden_Freddy("You", difficultie_golden_freddy, 0);
        Puppet p = new Puppet("You", difficultie_puppet, 0);


        this.Add_animatronic(b);
        this.Add_animatronic(f);
        this.Add_animatronic(c);
        this.Add_animatronic(f2);
        this.Add_animatronic(gf);
        this.Add_animatronic(p);
    }

    public void move_all_animatronics(Rooms_Graph rg){
        for (abstrac_animatronic animatronic : this.la) {
            animatronic.move(rg);
            animatronic.kill();
        }
    }

    public abstrac_animatronic get_puppet(){
        for (abstrac_animatronic a:la){
            if(a instanceof Puppet){
                return a;
            }
        }
        return null;

    }
}
