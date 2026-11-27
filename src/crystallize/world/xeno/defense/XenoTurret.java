package crystallize.world.xeno.defense;

import arc.audio.Sound;
import arc.graphics.Color;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.EnumSet;
import arc.struct.Seq;
import arc.graphics.g2d.TextureRegion;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import crystallize.world.draw.DrawXenoTurret;
import crystallize.world.xeno.XenoBlock;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.Mover;
import mindustry.entities.Predict;
import mindustry.entities.TargetPriority;
import mindustry.entities.UnitSorts;
import mindustry.entities.Units;
import mindustry.entities.Units.Sortf;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.pattern.ShootPattern;
import mindustry.gen.Building;
import mindustry.gen.Hitboxc;
import mindustry.gen.Sounds;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.world.draw.DrawBlock;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

import static mindustry.Vars.tilesize;

public class XenoTurret extends XenoBlock {
    public XenoTurret(String name) {
        super(name);
        outlineIcon = true;
        flags = EnumSet.of(BlockFlag.turret);
        group = BlockGroup.turrets;
        priority = TargetPriority.turret;
        rotate = true;
    }
    
    public float minRange = 0f;
    public float maxRange = 160f;
    
    public BulletType shootType;
    public float reload = 30f;
    public float inaccuracy = 0f;
    public float velocityRnd = 0f;
    public float shootCone = 8f;
    public float rotateSpeed = 5f;
    public ShootPattern shoot = new ShootPattern();
    public float shootX = 0f;
    public float shootY = -1f;
    public float xRand = 0f;
    public Effect shootEffect = Fx.none;
    public Sound shootSound = Sounds.shoot;
    
    public Effect chargeEffect = Fx.none;
    public Sound chargeSound = Sounds.none;
    public boolean moveWhileCharging = true;
    
    public float maxFireRateMultiplier = 1f;
    public float rampUpPerShot = 0.1f;
    public float rampDownSpeed = 0.005f;
    
    public boolean targetAir = true;
    public boolean targetGround = true;
    public Sortf unitSort = UnitSorts.closest;
    public float retargetTime = 20f;
    
    public float recoil = 1f;
    public float restitution = 0.02f;
    public Color heatColor = Pal.turretHeat;
    public float cooldownTime = 20f;
    public float shootWarmupSpeed = 0.1f;
    public float elevation = -1f;
    
    public DrawBlock drawer = new DrawXenoTurret();
    
    @Override
    public void init() {
        if (shootType == null) throw new RuntimeException("XenoTurret '" + name + "' chưa set shootType!");
        if (shootY < 0) shootY = size * tilesize / 2f;
        if (elevation < 0) elevation = size / 2f;
        clipSize = Math.max(clipSize, maxRange * 2f + tilesize * 2f);
        super.init();
    }

    @Override
    public void load() {
        super.load();
        drawer.load(this);
    }

    @Override
    public TextureRegion[] icons() {
        return drawer.finalIcons(this);
    }

    @Override
    public void getRegionsToOutline(Seq<TextureRegion> out) {
        drawer.getRegionsToOutline(this, out);
    }
    
    public class XenoTurretBuild extends XenoBuild {
        public float turretRotation = 90f;
        public float reloadCounter;
        public float retargetCounter;
        public float curRecoil;
        public float heat;
        public float charge;
        public float shootWarmup;
        public float rampUp;
        public int barrelCounter;
        public int queuedBullets;
        public Teamc target;
        
        public final Vec2 recoilOffset = new Vec2();

        private final Vec2 targetPos = new Vec2();
        
        public float drawrot() {
            return turretRotation - 90f;
        }
        
        public float progress() {
            return Mathf.clamp(reloadCounter / reload);
        }

        public boolean charging() {
            return queuedBullets > 0 && shoot.firstShotDelay > 0f;
        }

        public float fireRateMultiplier() {
            return Mathf.lerp(1f, maxFireRateMultiplier, rampUp);
        }
        
        public boolean inRange(Teamc t) {
            float dst = t.dst(this);
            float hit = 0f;
            if (t instanceof Hitboxc h) hit = h.hitSize() / 2f;
            else if (t instanceof Building b) hit = b.hitSize() / 2f;
            return dst <= maxRange + hit && dst >= minRange;
        }

        protected boolean validateTarget() {
            if (target == null) return false;
            if (Units.invalidateTarget(target, team, x, y)) return false;
            if (target instanceof Unit u && !u.checkTarget(targetAir, targetGround)) return false;
            if (target instanceof Building && !targetGround) return false;
            return inRange(target);
        }

        protected void findTarget() {
            target = Units.bestTarget(team, x, y, maxRange,
                    u -> !u.dead() && u.checkTarget(targetAir, targetGround) && inRange(u),
                    b -> targetGround && inRange(b),
                    unitSort);
        }

        protected boolean shouldTurn() {
            return moveWhileCharging || !charging();
        }

        @Override
        public void updateTile() {
            curRecoil = Mathf.lerpDelta(curRecoil, 0f, restitution);
            heat = Mathf.approachDelta(heat, 0f, 1f / cooldownTime);
            charge = charging() ? Mathf.approachDelta(charge, 1f, 1f / shoot.firstShotDelay) : 0f;
            recoilOffset.trns(turretRotation, -curRecoil);

            if (!validateTarget()) target = null;

            retargetCounter += delta();
            if (target == null && retargetCounter >= retargetTime) {
                retargetCounter = 0f;
                findTarget();
            }

            shootWarmup = Mathf.approachDelta(shootWarmup, target != null ? 1f : 0f, shootWarmupSpeed);
            
            if (target == null) rampUp = Mathf.approachDelta(rampUp, 0f, rampDownSpeed);
            
            if (reloadCounter < reload) reloadCounter += delta() * fireRateMultiplier();

            if (target == null) return;
            
            Vec2 predicted = Predict.intercept(this, target, shootType.speed);
            targetPos.set(predicted.x, predicted.y);
            if (targetPos.isZero()) targetPos.set(target.x(), target.y());

            float targetRot = angleTo(targetPos);
            if (shouldTurn()) {
                turretRotation = Angles.moveToward(turretRotation, targetRot, rotateSpeed * delta());
            }

            if (reloadCounter >= reload && !charging()
                    && Angles.within(turretRotation, targetRot, shootCone)) {
                shoot();
                reloadCounter %= reload;
            }
        }

        protected void shoot() {
            if (shoot.firstShotDelay > 0f) {
                float cx = x + Angles.trnsx(turretRotation - 90f, shootX, shootY);
                float cy = y + Angles.trnsy(turretRotation - 90f, shootX, shootY);
                chargeSound.at(cx, cy, Mathf.random(0.9f, 1.1f));
                chargeEffect.at(cx, cy, turretRotation);
            }
            
            shoot.shoot(barrelCounter, (xOffset, yOffset, angleOffset, delay, mover) -> {
                queuedBullets++;
                if (delay > 0f) {
                    Time.run(delay, () -> fireBullet(xOffset, yOffset, angleOffset, mover));
                } else {
                    fireBullet(xOffset, yOffset, angleOffset, mover);
                }
            }, () -> barrelCounter++);
            
            rampUp = Math.min(1f, rampUp + rampUpPerShot);
        }

        protected void fireBullet(float xOffset, float yOffset, float angleOffset, Mover mover) {
            queuedBullets--;
            if (!isValid()) return;

            float xSpread = Mathf.range(xRand);
            float bx = x + Angles.trnsx(turretRotation - 90f, shootX + xOffset + xSpread, shootY + yOffset);
            float by = y + Angles.trnsy(turretRotation - 90f, shootX + xOffset + xSpread, shootY + yOffset);
            float angle = turretRotation + angleOffset + Mathf.range(inaccuracy + shootType.inaccuracy);
            float vel = 1f - velocityRnd + Mathf.random(velocityRnd);
            
            float lifeScl = 1f;
            if (shootType.scaleLife) {
                float range = shootType.range;
                lifeScl = Mathf.clamp(Mathf.dst(bx, by, targetPos.x, targetPos.y) / range,
                        minRange / range, maxRange / range);
            }

            shootType.create(this, team, bx, by, angle, -1f, vel, lifeScl, null, mover, targetPos.x, targetPos.y);
            shootEffect.at(bx, by, turretRotation);
            shootSound.at(bx, by, Mathf.random(0.9f, 1.1f));

            curRecoil = recoil;
            heat = 1f;
        }

        @Override
        public void draw() {
            drawer.draw(this);
        }

        @Override
        public void drawLight() {
            super.drawLight();
            drawer.drawLight(this);
        }
        
        public float rotation() {
            return turretRotation;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(turretRotation);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            turretRotation = read.f();
        }
    }
}
