package insanemonster.insanemonster;

public enum MonsterAbilityType {

    LIGHTNING, EXPLODE, REFLECT, DEBUFF, BURN, GRAP, SPEEDY, DROP, HEAL;

    public static String getAbilityName(MonsterAbilityType abType){

        switch (abType){
            case LIGHTNING: return "번개";
            case EXPLODE: return "폭발";
            case DEBUFF: return "저주";
            case REFLECT: return "방어";
            case BURN: return "화염";
            case GRAP: return "속박";
            case SPEEDY: return "신속";
            case DROP: return "강탈";
            case HEAL: return "치유";

            default: return "노말";
        }

    }

}
