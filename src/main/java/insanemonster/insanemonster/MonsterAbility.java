package insanemonster.insanemonster;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MonsterAbility implements Listener {

    public static HashMap<LivingEntity, MonsterAbilityType> abilityMap = new HashMap<LivingEntity, MonsterAbilityType>();

    public MonsterAbility(){
        Bukkit.getPluginManager().registerEvents(this, InsaneMonster.server);
    }

    public MonsterAbilityType getRandomAbility(){
        int rd = Utility.getRandom(0, 8);
        return MonsterAbilityType.values()[rd];
    }
    
    public static void printRule(){
        Bukkit.broadcastMessage("§c인세인 모드가 활성화됐습니다.");
        Bukkit.broadcastMessage("§c이제부터 스폰되는 모든 엔티티는 능력을 갖게됩니다.");
        Bukkit.broadcastMessage("§b번개: §7죽을 때 자신을 죽인 플레이어에게 번개를 내리칩니다.");
        Bukkit.broadcastMessage("§b폭발: §7죽을 때 주위에 폭발을 발생시킵니다.");
        Bukkit.broadcastMessage("§b방어: §7원거리 투사체에 면역입니다.");
        Bukkit.broadcastMessage("§b저주: §7공격 당한 플레이어는 5초간 무작위 디버프를 받습니다.");
        Bukkit.broadcastMessage("§b화염: §7공격 당하거나 공격할 시 대상을 7초간 불태웁니다.");
        Bukkit.broadcastMessage("§b속박: §7자신을 공격한 플레이어를 조금씩 끌어당깁니다.");
        Bukkit.broadcastMessage("§b신속: §7이동속도 증가1 버프를 영구히 받습니다.");
        Bukkit.broadcastMessage("§b강탈: §7자신을 죽인 플레이어는 무작위로 소지 아이템을 1개 잃습니다.");
        Bukkit.broadcastMessage("§b치유: §7죽을 때 10칸 내 모든 엔티티를 치유합니다.");
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e){
        if(e.getEntity() instanceof LivingEntity){
            LivingEntity monster = (LivingEntity)e.getEntity();
            if(abilityMap.containsKey(monster)){ //죽은 몬스터가 능력 소지 시
                MonsterAbilityType abType = abilityMap.get(monster);
                abilityMap.remove(monster);

                LivingEntity killer = monster.getKiller();
                if(killer instanceof Player){
                    Player killerPlayer = (Player) killer;

                    if(abType.equals(MonsterAbilityType.LIGHTNING)){
                        killerPlayer.getWorld().spawnEntity(killerPlayer.getLocation(), EntityType.LIGHTNING);
                        killerPlayer.getWorld().playSound(killerPlayer.getLocation(), Sound.ENTITY_LIGHTNING_THUNDER, 1.0f, 1.0f);
                    } else if(abType.equals(MonsterAbilityType.EXPLODE)){
                        monster.getWorld().createExplosion(monster.getLocation(), 3.5f);
                    } else if(abType.equals(MonsterAbilityType.DROP)){

                        boolean isEmpty = true;
                        for(ItemStack tmpItem : killerPlayer.getInventory().getContents()){
                            if(tmpItem != null) {
                                isEmpty = false;
                                break;
                            }
                        }
                        if(!isEmpty){
                            int limit = 0;
                            int rdSlot = 0;
                            ItemStack item = null;
                            while(item == null){
                                rdSlot = Utility.getRandom(0, killerPlayer.getInventory().getSize() - 1);
                                item = killerPlayer.getInventory().getItem(rdSlot);
                                limit++;
                                if(limit > 1000) {
                                    break;
                                }
                            }
                            if(item != null){
                                //killerPlayer.getWorld().dropItem(killerPlayer.getLocation().clone().add(0,2,0),item);
                                item.setAmount(item.getAmount()-1);
                                killerPlayer.getInventory().setItem(rdSlot, item);
                                killerPlayer.sendMessage("§c강탈 속성 몬스터를 죽여 아이템을 잃었습니다.");
                                killerPlayer.getWorld().playSound(killerPlayer.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.5f);
                            }
                        }
                    } else if(abType.equals(MonsterAbilityType.HEAL)){
                        List<Entity> entities = monster.getNearbyEntities(10,10,10);
                        for(Entity tmpEntity : entities){
                            if(tmpEntity == null) continue;
                            if(tmpEntity instanceof LivingEntity){
                                if(tmpEntity instanceof Player) continue;; //플레이어는 치유 안해줌
                                LivingEntity lvEntity = (LivingEntity)tmpEntity;
                                if(lvEntity.equals(monster)) continue; //자기 자신도 치유 안해줌
                                double maxHealth = lvEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                                if(MonsterAbility.abilityMap.containsKey(lvEntity)){
                                    //Bukkit.getLogger().info(maxHealth+"");
                                    lvEntity.setHealth(maxHealth);
                                    lvEntity.getWorld().playSound(lvEntity.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
                                    MonsterAbilityType tmpAbType = MonsterAbility.abilityMap.get(lvEntity);
                                    String abName = MonsterAbilityType.getAbilityName(tmpAbType);
                                    lvEntity.setCustomName("§f[§b"+abName+"§f] §cHP: §6"+((int)maxHealth));
                                } else {
                                    lvEntity.setCustomName("§cHP: §6"+(int)maxHealth);
                                }

                            }
                        }
                    }

                }

            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e){
        if(e.getEntity() instanceof LivingEntity){ //엔티티가 공격 당했을 때
            
            if(e.getEntity() instanceof Player) { //플레이어가 공격 당했을 때
                Player victimPlayer = (Player) e.getEntity();
                if (e.getDamager() instanceof LivingEntity) {
                    LivingEntity monster = (LivingEntity) e.getDamager();
                    if (abilityMap.containsKey(monster)) { //공격 몬스터가 능력 소지 시
                        MonsterAbilityType abType = abilityMap.get(monster);
                        if (abType.equals(MonsterAbilityType.BURN)) {
                            victimPlayer.setFireTicks(140);
                        } else if (abType.equals(MonsterAbilityType.DEBUFF)) {
                            PotionEffectType debuff;
                            int rd = Utility.getRandom(0, 8);
                            switch (rd) {
                                case 0:
                                    debuff = PotionEffectType.BLINDNESS; break;
                                case 1:
                                    debuff = PotionEffectType.CONFUSION; break;
                                case 2:
                                    debuff = PotionEffectType.HUNGER; break;
                                case 3:
                                    debuff = PotionEffectType.POISON; break;
                                case 4:
                                    debuff = PotionEffectType.SLOW; break;
                                case 5:
                                    debuff = PotionEffectType.SLOW_DIGGING; break;
                                case 6:
                                    debuff = PotionEffectType.WITHER; break;
                                case 7:
                                    debuff = PotionEffectType.WEAKNESS; break;
                                case 8:
                                    debuff = PotionEffectType.UNLUCK; break;

                                default:
                                    debuff = PotionEffectType.BLINDNESS;
                            }
                            victimPlayer.addPotionEffect(new PotionEffect(debuff, 100, Utility.getRandom(0, 2)));
                        }
                    }
                }
            } else { //몬스터가 맞았을 시
                LivingEntity monster = (LivingEntity) e.getEntity();
                if(abilityMap.containsKey(monster)){ //맞은 몬스터가 능력 소지 시
                    MonsterAbilityType abType = abilityMap.get(monster);

                    if(e.getDamager() instanceof LivingEntity){
                        LivingEntity damager = (LivingEntity) e.getDamager();
                        if(damager instanceof Player){
                            Player damagerPlayer = (Player) damager;

                            if(abType.equals(MonsterAbilityType.BURN)){
                                damagerPlayer.setFireTicks(140);
                            } else if(abType.equals(MonsterAbilityType.GRAP)){
                                Vector v = monster.getLocation().toVector().subtract(damagerPlayer.getLocation().toVector());
                                v.normalize();
                                v.multiply(0.5f);
                                damagerPlayer.setVelocity(v);
                                damagerPlayer.getWorld().playSound(damagerPlayer.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1.0f, 0.5f);
                            }
                        }

                    } else {
                        Entity damagerEntity = e.getDamager();
                        if(abType.equals(MonsterAbilityType.REFLECT)){ //반사
                            if(damagerEntity.getType() == EntityType.ARROW){
                                Arrow arrow = (Arrow)damagerEntity;
                                e.setCancelled(true);
                                damagerEntity.setVelocity(damagerEntity.getVelocity().multiply(-5));
                                if(arrow.getShooter() instanceof Player){
                                    Player shooter = (Player) arrow.getShooter();
                                    shooter.getWorld().playSound(shooter.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 2.0f);
                                }
                            } else if(damagerEntity.getType() == EntityType.SNOWBALL){
                                Snowball snowball = (Snowball)damagerEntity;
                                e.setCancelled(true);
                                damagerEntity.setVelocity(damagerEntity.getVelocity().multiply(-5));
                                if(snowball.getShooter() instanceof Player){
                                    Player shooter = (Player) snowball.getShooter();
                                    shooter.getWorld().playSound(shooter.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 2.0f);
                                }
                            } else if(damagerEntity.getType() == EntityType.EGG){
                                Egg egg = (Egg)damagerEntity;
                                e.setCancelled(true);
                                damagerEntity.setVelocity(damagerEntity.getVelocity().multiply(-5));
                                if(egg.getShooter() instanceof Player){
                                    Player shooter = (Player) egg.getShooter();
                                    shooter.getWorld().playSound(shooter.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 2.0f);
                                }
                            }
                        } else if(abType.equals(MonsterAbilityType.GRAP)){
                            if(damagerEntity instanceof Projectile){
                                Projectile projectile = (Projectile) damagerEntity;
                                if(projectile.getShooter() instanceof Player){
                                    Player shooter = (Player) projectile.getShooter();
                                    Vector v = monster.getLocation().toVector().subtract(shooter.getLocation().toVector());
                                    v.normalize();
                                    v.multiply(0.5f);
                                    shooter.setVelocity(v);
                                    shooter.getWorld().playSound(shooter.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1.0f, 0.5f);
                                }
                            }
                        }
                    }
                }
            }
           
        }

    }


}
