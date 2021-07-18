package insanemonster.insanemonster;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;

public final class InsaneMonster extends JavaPlugin implements Listener {

    public static Plugin server;
    public boolean isInsaneMod = false;
    public MonsterAbility monsterAbility;

    @Override
    public void onEnable() {
        // Plugin startup logic

        server = this;

        monsterAbility = new MonsterAbility();

        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getLogger().info("난이도 상향 플러그인 로드됨");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.getLogger().info("난이도 상향 플러그인 로드됨");
    }

    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
        if(commandLabel.equalsIgnoreCase("insaneMod")){
            if(!isInsaneMod){
                isInsaneMod = true;
                sender.sendMessage("§c그들이 더욱 더 강해집니다...");
                MonsterAbility.printRule();
            } else {
                isInsaneMod = false;
                sender.sendMessage("§c그들이 약해졌습니다.");
            }
        }
        return true;
    }

    @EventHandler
    public void onEntityDamaged(EntityCombustEvent e){
        e.setCancelled(true); //몬스터 햇빛 뎀 없애기
    }

    @EventHandler
    public void onEntitySpawn(CreatureSpawnEvent e){
        if(!(e.getEntity() instanceof Player)){ //플레이어가 아닌 엔티티일 시
            LivingEntity monster = e.getEntity();
            AttributeInstance attr = monster.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            double newHp = monster.getHealth()*5;
            attr.setBaseValue(newHp); //체력 5배
            monster.setHealth(newHp);
            monster.setCustomNameVisible(true);
            if(isInsaneMod){
                MonsterAbilityType abType = monsterAbility.getRandomAbility();
                MonsterAbility.abilityMap.put(monster, abType);
                String abName = MonsterAbilityType.getAbilityName(abType);
                monster.setCustomName("§f[§b"+abName+"§f] §cHP: §6"+((int)monster.getHealth()));
                if(abType.equals(MonsterAbilityType.SPEEDY)){
                    monster.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 72000, 0, false, false));
                }
            } else {
                monster.setCustomName("§cHP: §6"+newHp);
            }
        }
    }


    @EventHandler
    public void onEntityDamage(EntityDamageEvent e){ //이름을 체력으로 해서 보이기
        if(e.getEntity() instanceof LivingEntity){
            LivingEntity monster = (LivingEntity) e.getEntity();
            int showHp = (int)(monster.getHealth()-e.getDamage());
            if(showHp < 0) showHp = 0;
            if(isInsaneMod){
                if(MonsterAbility.abilityMap.containsKey(monster)){
                    MonsterAbilityType abType = MonsterAbility.abilityMap.get(monster);
                    String abName = MonsterAbilityType.getAbilityName(abType);

                    monster.setCustomName("§f[§b"+abName+"§f] §cHP: §6"+(showHp));
                }
            } else {
                monster.setCustomName("§cHP: §6"+(showHp));
            }
        }
    }

    @EventHandler
    public void onEntityDamagedByEntity(EntityDamageByEntityEvent e){
        if(e.getDamager() instanceof Player){
            Player d = (Player)e.getDamager();
            if(e.getEntity() instanceof LivingEntity){
                LivingEntity monster = (LivingEntity) e.getEntity();
                //d.sendMessage("hp: "+monster.getHealth()); //몬스터 체력 확인용
            }
        }
    }
}
