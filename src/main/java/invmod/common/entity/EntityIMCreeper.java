package invmod.common.entity;

import invmod.common.INotifyTask;
import invmod.common.mod_Invasion;
import invmod.common.entity.ai.EntityAIAttackNexus;
import invmod.common.entity.ai.EntityAICreeperIMSwell;
import invmod.common.entity.ai.EntityAIGoToNexus;
import invmod.common.entity.ai.EntityAIKillEntity;
import invmod.common.entity.ai.EntityAISimpleTarget;
import invmod.common.entity.ai.EntityAITargetRetaliate;
import invmod.common.entity.ai.EntityAIWaitForEngy;
import invmod.common.entity.ai.EntityAIWanderIM;
import invmod.common.nexus.INexusAccess;
import invmod.common.util.ExplosionUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

//NOOB HAUS  this.c.addTask(0, new EntityAISwimming(this));   c = this.tasks...
//Noob HAUS  this.d.addTask(2, new EntityAIHurtByTarget(this, false));   d = this.targetTasks

public class EntityIMCreeper extends EntityIMMob
{
    private int timeSinceIgnited;
    private int lastActiveTime;
    private boolean explosionDeath;
    private boolean commitToExplode;
    private int explodeDirection;
    private int tier;

    public EntityIMCreeper(World world)
    {
        this(world, null);
    }

    public EntityIMCreeper(World world, INexusAccess nexus)
    {
        super(world, nexus);
        this.setName("Creeper");
        this.setGender(0);
        this.tier = 1;
        setBaseMoveSpeedStat(0.21F);
        setMaxHealthAndHealth(mod_Invasion.getMobHealth(this));
        setAI();
    }

    private void setAI()
    {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAICreeperIMSwell(this));
        this.tasks.addTask(2, new EntityAIAvoidEntity(this, EntityOcelot.class, 6.0F, 0.25D, 0.300000011920929D));
        this.tasks.addTask(3, new EntityAIKillEntity(this, EntityPlayer.class, 40));
        this.tasks.addTask(3, new EntityAIKillEntity(this, EntityPlayerMP.class, 40));
        this.tasks.addTask(4, new EntityAIAttackNexus(this));
        this.tasks.addTask(5, new EntityAIWaitForEngy(this, 4.0F, true));
        this.tasks.addTask(6, new EntityAIKillEntity(this, EntityLiving.class, 40));
        this.tasks.addTask(7, new EntityAIGoToNexus(this));
        this.tasks.addTask(8, new EntityAIWanderIM(this));
        this.tasks.addTask(9, new EntityAIWatchClosest(this, EntityPlayer.class, 4.8F));
        this.tasks.addTask(9, new EntityAILookIdle(this));
        this.targetTasks.addTask(0, new EntityAITargetRetaliate(this, EntityLiving.class, 12.0F));
        this.targetTasks.addTask(1, new EntityAISimpleTarget(this, EntityPlayer.class, this.getSenseRange(), false));
        this.targetTasks.addTask(2, new EntityAISimpleTarget(this, EntityPlayer.class, this.getAggroRange(), true));
        this.targetTasks.addTask(3, new EntityAIHurtByTarget(this, false));
    }

    @Override
    public void updateAITick()
    {
        super.updateAITick();
    }

    @Override
    public boolean isAIEnabled()
    {
        return true;
    }

    @Override
    public boolean onPathBlocked(Path path, INotifyTask notifee)
    {
        if (!path.isFinished())
        {
            PathNode node = path.getPathPointFromIndex(path.getCurrentPathIndex());
            double dX = node.xCoord + 0.5D - this.posX;
            double dZ = node.zCoord + 0.5D - this.posZ;
            float facing = (float)(Math.atan2(dZ, dX) * 180.0D / Math.PI) - 90.0F;

            if (facing < 0.0F)
            {
                facing += 360.0F;
            }

            facing %= 360.0F;

            if ((facing >= 45.0F) && (facing < 135.0F))
            {
                this.explodeDirection = 1;
            }
            else if ((facing >= 135.0F) && (facing < 225.0F))
            {
                this.explodeDirection = 3;
            }
            else if ((facing >= 225.0F) && (facing < 315.0F))
            {
                this.explodeDirection = 0;
            }
            else
            {
                this.explodeDirection = 2;
            }

            setCreeperState(1);
            this.commitToExplode = true;
        }

        return false;
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
        this.dataWatcher.addObject(16, Byte.valueOf((byte) - 1));
        this.dataWatcher.addObject(17, Byte.valueOf((byte)0));
    }

    @Override
    public void onUpdate()
    {
        if (this.explosionDeath)
        {
            doExplosion();
            setDead();
        }
        else if (isEntityAlive())
        {
            this.lastActiveTime = this.timeSinceIgnited;
            int state = getCreeperState();

            if (state > 0)
            {
                if (this.commitToExplode)
                {
                    getMoveHelper().setMoveTo(this.posX + invmod.common.util.CoordsInt.offsetAdjX[this.explodeDirection], this.posY, this.posZ + invmod.common.util.CoordsInt.offsetAdjZ[this.explodeDirection], 0.0D);
                }

                if (this.timeSinceIgnited == 0)
                {
                    this.worldObj.playSoundAtEntity(this, "random.fuse", 1.0F, 0.5F);
                }
            }

            this.timeSinceIgnited += state;

            if (this.timeSinceIgnited < 0)
            {
                this.timeSinceIgnited = 0;
            }

            if (this.timeSinceIgnited >= 30)
            {
                this.timeSinceIgnited = 30;
                this.explosionDeath = true;
            }
        }

        super.onUpdate();
    }

    @Override
    protected String getHurtSound()
    {
        return "mob.creeper.say";
    }

    @Override
    protected String getDeathSound()
    {
        return "mob.creeper.death";
    }

    @Override
    public String getSpecies()
    {
        return "Creeper";
    }

    @Override
    public int getTier()
    {
        return this.tier;
    }

    @Override
    public void onDeath(DamageSource par1DamageSource)
    {
        super.onDeath(par1DamageSource);

        if ((par1DamageSource.getEntity() instanceof EntitySkeleton))
        {
            dropItem(Item.record13.itemID + this.rand.nextInt(10), 1);
        }
    }

    @Override
    public boolean attackEntityAsMob(Entity par1Entity)
    {
        return true;
    }

    public float setCreeperFlashTime(float par1)
    {
        return (this.lastActiveTime + (this.timeSinceIgnited - this.lastActiveTime) * par1) / 28.0F;
    }

    @Override
    public float getBlockPathCost(PathNode prevNode, PathNode node, IBlockAccess terrainMap)
    {
        int id = terrainMap.getBlockId(node.xCoord, node.yCoord, node.zCoord);

        if ((id > 0) && (!Block.blocksList[id].getBlocksMovement(terrainMap, node.xCoord, node.yCoord, node.zCoord)) && (id != mod_Invasion.blockNexus.blockID))
        {
            return prevNode.distanceTo(node) * 12.0F;
        }

        return super.getBlockPathCost(prevNode, node, terrainMap);
    }

    @Override
    public String toString()
    {
        return "IMCreeper-T" + this.getTier();
    }

    @Override
    protected int getDropItemId()
    {
        return Item.gunpowder.itemID;
    }

    protected void doExplosion()
    {
    	
        boolean mobgriefing = this.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing");
        Explosion explosion = new Explosion(this.worldObj, this, this.posX, this.posY + 1.0D, this.posZ, 2.1F);
        explosion.isFlaming = false;
        explosion.isSmoking = mobgriefing;
        if(!worldObj.isRemote){
        explosion.doExplosionA();
        }
        ExplosionUtil.doExplosionB(this.worldObj, explosion, true);
    }

    public int getCreeperState()
    {
        return this.dataWatcher.getWatchableObjectByte(16);
    }

    public void setCreeperState(int state)
    {
        if ((this.commitToExplode) && (state != 1))
        {
            return;
        }

        this.dataWatcher.updateObject(16, Byte.valueOf((byte)state));
    }
}