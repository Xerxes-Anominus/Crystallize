package xean.content;

import mindustry.type.Item;

public class Cryst_Items {
    public static Item
    vanadium, tantalum, niobium,
    xenoShard;
    
    public static void load() {
        vanadium = new Item("vanadium") {{
            hardness = 2;
        }};
        tantalum = new Item("tantalum") {{
            hardness = 3;
        }};
        niobium = new Item("niobium") {{
            flammability = 0.05f;
        }};
        
        xenoShard = new Item("xenoShard") {{
            charge = 2.1f;
            buildable = false;
        }};
    }
}