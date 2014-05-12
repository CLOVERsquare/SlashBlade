package mods.flammpfeil.slashblade;

import com.google.common.collect.Multimap;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.IThrowableEntity;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.command.IEntitySelector;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;

public class ItemSlashBlade extends ItemSword {

	public static IEntitySelector AttackableSelector = new EntitySelectorAttackable();
	public static IEntitySelector DestructableSelector = new EntitySelectorDestructable();


	private static ResourceLocation texture = new ResourceLocation("flammpfeil.slashblade","model/blade.png");
	public ResourceLocation getModelTexture(){
		return texture;
	}
    static public Map<String,ResourceLocation> textureMap = new HashMap<String, ResourceLocation>();

    final public static String TextureNameStr = "TextureName";
    static public ResourceLocation getModelTexture(ItemStack par1ItemStack){
        NBTTagCompound tag = getItemTagCompound(par1ItemStack);
        if(tag.hasKey(TextureNameStr)){
            String textureName = tag.getString(TextureNameStr);
            ResourceLocation loc;
            if(!textureMap.containsKey(textureName))
            {
                loc = new ResourceLocation("flammpfeil.slashblade","model/" + textureName + ".png");
                textureMap.put(textureName,loc);
            }else{
                loc = textureMap.get(textureName);
            }
            return loc;
        }
        return ((ItemSlashBlade)par1ItemStack.getItem()).getModelTexture();
    }


    private ResourceLocation model =  new ResourceLocation("flammpfeil.slashblade","model/blade.obj");
    public ResourceLocation getModel(){ return model; }
    static public Map<String,ResourceLocation> modelMap = new HashMap<String, ResourceLocation>();

    final public static String ModelNameStr = "ModelName";
    static public ResourceLocation getModelLocation(ItemStack par1ItemStack){
        NBTTagCompound tag = getItemTagCompound(par1ItemStack);
        if(tag.hasKey(ModelNameStr)){
            String modelName = tag.getString(ModelNameStr);
            ResourceLocation loc;
            if(!modelMap.containsKey(modelName))
            {
                loc = new ResourceLocation("flammpfeil.slashblade","model/" + modelName + ".obj");
                modelMap.put(modelName,loc);
            }else{
                loc = modelMap.get(modelName);
            }
            return loc;
        }
        return ((ItemSlashBlade)par1ItemStack.getItem()).getModel();
    }



    @Override
    public EnumAction getItemUseAction(ItemStack par1ItemStack) {
        return EnumAction.none;
    }

    static final class EntitySelectorAttackable implements IEntitySelector
	{
	    public boolean isEntityApplicable(Entity par1Entity)
	    {
	    	boolean result = false;

			String entityStr = EntityList.getEntityString(par1Entity);
			//含む
			if(((entityStr != null && SlashBlade.manager.attackableTargets.containsKey(entityStr) && SlashBlade.manager.attackableTargets.get(entityStr))
				|| par1Entity instanceof EntityDragonPart
				))
				result = par1Entity.isEntityAlive();

	        return result;
	    }
	}

	static final class EntitySelectorDestructable implements IEntitySelector
	{
	    public boolean isEntityApplicable(Entity par1Entity)
	    {
	    	boolean result = false;

			if(par1Entity instanceof IProjectile
					|| par1Entity instanceof EntityTNTPrimed
					|| par1Entity instanceof EntityFireball
					|| par1Entity instanceof IThrowableEntity){
				result = par1Entity.isEntityAlive();
			}else{
				String className = par1Entity.getClass().getSimpleName();
				if(SlashBlade.manager.destructableTargets.containsKey(className) && SlashBlade.manager.destructableTargets.get(className))
					result = par1Entity.isEntityAlive();
			}

	        return result;
	    }
	}

    public static final String adjustXStr = "adjustX";
    public static final String adjustYStr = "adjustY";
    public static final String adjustZStr = "adjustZ";

	public static final String comboSeqStr = "comboSeq";
	public static final String isBrokenStr = "isBroken";
	public static final String onClickStr = "onClick";
	public static final String lastPosHashStr = "lastPosHash";
	public static final String lastActionTimeStr = "lastActionTime";
	public static final String onJumpAttackedStr = "onJumpAttacked";
	public static final String attackAmplifierStr = "attackAmplifier";
	public static final String killCountStr = "killCount";
	public static final String proudSoulStr = "ProudSoul";
    public static final String TargetEntityStr = "TargetEntity";
    public static final String isNoScabbardStr = "isNoScabbard";
    public static final String isSealedStr = "isSealed";
    public static final String isChargedStr = "isCharged";
    public static final String SpecialAttackTypeStr = "SpecialAttackType";
    public static final String RepairCounterStr = "RepairCounter";
    public static final String StandbyRenderTypeStr = "StandbyRenderType";


	public static int AnvilRepairBonus = 100;

	public static void setComboSequence(NBTTagCompound tag,ComboSequence comboSeq){
		tag.setInteger(comboSeqStr, comboSeq.ordinal());
        if(comboSeq == ComboSequence.None){
            tag.setBoolean(isChargedStr,false);
        }
	}

	public static ComboSequence getComboSequence(NBTTagCompound tag){
		return ComboSequence.get(tag.getInteger(comboSeqStr));
	}


	private static ArrayList<ComboSequence> Seqs = new ArrayList<ItemSlashBlade.ComboSequence>();
    public enum ComboSequence
	{
    	None(true,0.0f,0.0f,false,0),
    	Saya1(true,200.0f,5.0f,false,6),
    	Saya2(true,-200.0f,5.0f,false,12),
    	Battou(false,240.0f,0.0f,false,12),
    	Noutou(false,-210.0f,10.0f,false,5),
    	Kiriage(false,260.0f,70.0f,false,20),
    	Kiriorosi(false,-260.0f,90.0f,false,12),
    	SlashDim(false,-220.0f,10.0f,true,8),
    	Iai(false,240.0f,0.0f,false,8),
    	;

	    /**
	     * ordinal : コンボ進行ID
	     */

	    /**
	     * 抜刀フラグ trueなら鞘打ち
	     */
	    public boolean useScabbard;

	    /**
	     * 振り幅 マイナスは振り切った状態から逆に振る
	     */
	    public float swingAmplitude;

	    /**
	     * 振る方向 360度
	     */
	    public float swingDirection;

	    /**
	     * チャージエフェクト
	     */
	    public boolean isCharged;

	    public int comboResetTicks;

	    /**
	     *
	     * @param useScabbard true:鞘も動く
	     * @param swingAmplitude 振り幅 マイナスは振り切った状態から逆に振る
	     * @param swingDirection 振る角度
	     * @param isCharged チャージエフェクト有無
	     */
	    private ComboSequence(boolean useScabbard, float swingAmplitude, float swingDirection, boolean isCharged,int comboResetTicks)
	    {
	    	Seqs.add(this.ordinal(), this);

	    	this.useScabbard = useScabbard;
	    	this.swingAmplitude = swingAmplitude;
	    	this.swingDirection = swingDirection;
	    	this.isCharged = isCharged;
	    	this.comboResetTicks = comboResetTicks;
	    }

	    public static ComboSequence get(int ordinal){
	    	return Seqs.get(ordinal);
	    }
	}

	static public int RequiredChargeTick = 15;
	static public int ComboInterval = 4;


	protected void damageItem(int damage, ItemStack par1ItemStack, EntityLivingBase par3EntityLivingBase){

		NBTTagCompound tag = getItemTagCompound(par1ItemStack);

		if(par1ItemStack.getItemDamage() == 0){
			tag.setBoolean(isBrokenStr, false);
		}

		if(par1ItemStack.attemptDamageItem(damage, par3EntityLivingBase.getRNG())){
			par1ItemStack.setItemDamage(par1ItemStack.getMaxDamage());

			if(!tag.getBoolean(isBrokenStr)){

				tag.setBoolean(isBrokenStr, true);
				par3EntityLivingBase.renderBrokenItemStack(par1ItemStack);

				if(!par3EntityLivingBase.worldObj.isRemote){
					int proudSouls = tag.getInteger(proudSoulStr);
					int count = 0;
					if(proudSouls > 1000){
						count = (proudSouls / 3) / 100;
						proudSouls = (proudSouls/3) * 2;
					}else{
						count = proudSouls / 100;
						proudSouls = proudSouls % 100;
					}
					count++;

					proudSouls = Math.max(0,Math.min(999999999, proudSouls));
					tag.setInteger(proudSoulStr, proudSouls);
					par3EntityLivingBase.entityDropItem(GameRegistry.findItemStack(SlashBlade.modid, SlashBlade.ProudSoulStr, count), 0.0F);
				}
			}
		}
	}

	public EntityLivingBase setDaunting(EntityLivingBase entity){
		if(!entity.worldObj.isRemote){
			entity.addPotionEffect(new PotionEffect(Potion.moveSlowdown.getId(),10,30,true));
			entity.attackTime = 20;
		}
		return entity;
	}

    /**
     * Current implementations of this method in child classes do not use the entry argument beside ev. They just raise
     * the damage on the stack.
     */
	@Override
    public boolean hitEntity(ItemStack par1ItemStack, EntityLivingBase par2EntityLivingBase, EntityLivingBase par3EntityLivingBase)
    {

		int damage = 1;

		NBTTagCompound tag = getItemTagCompound(par1ItemStack);

		if(!par2EntityLivingBase.isEntityAlive() && par2EntityLivingBase.deathTime == 0){
			int killCount = tag.getInteger(killCountStr) + 1;
			if(killCount <= 999999999){
				tag.setInteger(killCountStr, killCount);
			}
		}

    	ComboSequence comboSec = getComboSequence(tag);

    	switch (comboSec) {
		case Kiriage:
			par2EntityLivingBase.onGround = false;
			par2EntityLivingBase.motionX = 0;
			par2EntityLivingBase.motionY = 0;
			par2EntityLivingBase.motionZ = 0;
			par2EntityLivingBase.addVelocity(0.0, 0.7D,0.0);

			setDaunting(par2EntityLivingBase);
			break;

		case Kiriorosi:

			if(0 < par2EntityLivingBase.motionY)
				par2EntityLivingBase.motionY = 0;

			par2EntityLivingBase.fallDistance += 4;


			{
				float knockbackFactor = 0.5f;
				par2EntityLivingBase.addVelocity((double)(-MathHelper.sin(par3EntityLivingBase.rotationYaw * (float)Math.PI / 180.0F) * (float)knockbackFactor * 0.5F), -0.2D, (double)(MathHelper.cos(par3EntityLivingBase.rotationYaw * (float)Math.PI / 180.0F) * (float)knockbackFactor * 0.5F));
			}

			par2EntityLivingBase.hurtResistantTime = 0;

			break;

		case Battou:

			{
				float knockbackFactor = 0f;
				if(par2EntityLivingBase instanceof EntityLivingBase)
					knockbackFactor = EnchantmentHelper.getKnockbackModifier(par3EntityLivingBase, par2EntityLivingBase);

				if(!(0 < knockbackFactor))
					knockbackFactor = 1.5f;

				par2EntityLivingBase.motionX = 0;
				par2EntityLivingBase.motionY = 0;
				par2EntityLivingBase.motionZ = 0;
				par2EntityLivingBase.addVelocity(
                        (double)(-MathHelper.sin(par3EntityLivingBase.rotationYaw * (float)Math.PI / 180.0F) * (float)knockbackFactor * 0.5F),
                        0.2D,
                        (double)(MathHelper.cos(par3EntityLivingBase.rotationYaw * (float)Math.PI / 180.0F) * (float)knockbackFactor * 0.5F));
			}

			break;

		case Iai:
			par2EntityLivingBase.motionX = 0;
			par2EntityLivingBase.motionY = 0;
			par2EntityLivingBase.motionZ = 0;
			par2EntityLivingBase.addVelocity(0.0, 0.3D,0.0);

			setDaunting(par2EntityLivingBase);

			break;

		case Saya1:
		case Saya2:

			par2EntityLivingBase.motionX = 0;
			par2EntityLivingBase.motionY = 0;
			par2EntityLivingBase.motionZ = 0;

			setDaunting(par2EntityLivingBase);

			damage = 0;
			break;

        case SlashDim:

			int level = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, par1ItemStack);
			if(0 < level){
    			par2EntityLivingBase.addVelocity(
    					(double)(MathHelper.sin(par3EntityLivingBase.rotationYaw * (float)Math.PI / 180.0F) * (float)level * 0.5F),
    					0.2D,
    					(double)(-MathHelper.cos(par3EntityLivingBase.rotationYaw * (float)Math.PI / 180.0F) * (float)level * 0.5F));
        	}

			setDaunting(par2EntityLivingBase);
            break;

		default:
			break;
		}

    	if(0 < damage)
    		damageItem(damage, par1ItemStack,par3EntityLivingBase);

		return true;
    }


	@Override
    public boolean onBlockDestroyed(ItemStack par1ItemStack, World par2World, Block par3, int par4, int par5, int par6, EntityLivingBase par7EntityLivingBase)
    {
        if ((double)par3.getBlockHardness(par2World, par4, par5, par6) != 0.0D)
        {
        	damageItem(1, par1ItemStack,par7EntityLivingBase);
        }

        return true;
    }

    /**
     * Gets a map of item attribute modifiers, used by ItemSword to increase hit damage.
     */
	@Override
    public Multimap getItemAttributeModifiers()
    {
        Multimap multimap = super.getItemAttributeModifiers();
        multimap.removeAll(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName());
        multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e, "Weapon modifier", (double)baseAttackModifiers, 0));
        return multimap;
    }

	@Override
	public void registerIcons(IIconRegister par1IconRegister) {
        this.itemIcon = par1IconRegister.registerIcon("flammpfeil.slashblade:proudsoul");
	}

	public float baseAttackModifiers = 4.0f;

	public ItemSlashBlade(Item.ToolMaterial par2EnumToolMaterial,float baseAttackModifiers) {
		super(par2EnumToolMaterial);
        this.setMaxDamage(50);
        this.baseAttackModifiers = baseAttackModifiers;
	}

    static NBTTagCompound getItemTagCompound(ItemStack stack){
		NBTTagCompound tag;
		if(stack.hasTagCompound()){
			tag = stack.getTagCompound();
		}else{
			tag = new NBTTagCompound();
			stack.setTagCompound(tag);
		}

		return tag;
	}

	public ComboSequence getNextComboSeq(ItemStack itemStack, ComboSequence current, boolean isRightClick, EntityPlayer player){
		ComboSequence result = ComboSequence.None;

        EnumSet<SwordType> types = getSwordType(itemStack);
        if(types.contains(SwordType.NoScabbard)){
            result = ComboSequence.None;
        }else if(!player.onGround){
			switch (current) {
			case Iai:
				result = ComboSequence.Battou;
				break;

			default:
				result = ComboSequence.Iai;
				break;
			}

		}else if(isRightClick){

			switch (current) {

			case Saya1:
				result = ComboSequence.Saya2;
				break;

			case Saya2:
				result = ComboSequence.Battou;
				break;

			case Kiriage:
				result = ComboSequence.Kiriorosi;
				break;

			default:
				result = ComboSequence.Saya1;

				break;
			}
		}else{
			switch (current) {

			case Kiriage:
				result = ComboSequence.Kiriorosi;
				break;

			default:
				result = ComboSequence.Kiriage;
				break;
			}
		}

		setPlayerEffect(itemStack,result,player);

		return result;
	}


	public void setPlayerEffect(ItemStack itemStack, ComboSequence current, EntityPlayer player){

		EnumSet<SwordType> swordType = getSwordType(itemStack);

		NBTTagCompound tag = getItemTagCompound(itemStack);

		switch (current) {
		case Iai:
			player.fallDistance = 0;
			if(!tag.getBoolean(onJumpAttackedStr)){
				player.motionY = 0;
				player.addVelocity(0.0, 0.3D,0.0);


                int level = 1 + EnchantmentHelper.getEnchantmentLevel(Enchantment.featherFalling.effectId, itemStack);
                player.fallDistance *= Math.max(0,(4.5-level)/5.0);
			}
			break;

		case Battou:
			if (!player.onGround){
				if(!tag.getBoolean(onJumpAttackedStr)){
					player.motionY = 0;
					player.addVelocity(0.0, 0.2D,0.0);
					tag.setBoolean(onJumpAttackedStr, true);


	                int level = 1 + EnchantmentHelper.getEnchantmentLevel(Enchantment.featherFalling.effectId, itemStack);
	                player.fallDistance *= Math.max(0,(4.5-level)/5.0);
	                
				}
			}

			if(swordType.containsAll(SwordType.BewitchedPerfect)){
				Random rand =  player.getRNG();
				for(int spread = 0 ; spread < 12 ;spread ++){
					float xSp = rand.nextFloat() * 2 - 1.0f;
					float zSp = rand.nextFloat() * 2 - 1.0f;
					xSp += 0.2 * Math.signum(xSp);
					zSp += 0.2 * Math.signum(zSp);
					player.worldObj.spawnParticle("largeexplode",
							player.posX + 3.0f*xSp,
							player.posY,
							player.posZ + 3.0f*zSp,
		            		1.0, 1.0, 1.0);
				}
			}

			break;
		default:

			break;
		}

        if(!current.useScabbard){
            if(tag.getBoolean(isChargedStr)){
                tag.setBoolean(isChargedStr,false);
                if(player instanceof EntityPlayer){
                    procDrive(itemStack,player.worldObj,(EntityPlayer)player,current);
                }
            }
        }
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player,
			Entity entity) {

		NBTTagCompound tag = getItemTagCompound(stack);

		if(!tag.getBoolean(onClickStr) ){ // onClick中は rightClickなので無視
	        if (entity.canAttackWithItem()){
	            if (!entity.hitByEntity(player) || entity instanceof EntityLivingBase){

	            	//左クリック攻撃は無敵時間を考慮する コンボインターバルが入っている
	            	if(entity instanceof EntityLivingBase
	            			&& ((EntityLivingBase)entity).maxHurtTime != 0 && ((ComboInterval + 2) > ((EntityLivingBase)entity).maxHurtTime - ((EntityLivingBase)entity).hurtTime))
	            	{
	            		//腕振りしない
	            		player.swingProgressInt = 0;
	            		player.swingProgress = 0.0f;
	            		player.isSwingInProgress = false;
	            		return true;
	            	}

		        	ComboSequence comboSec = getComboSequence(tag);

		        	comboSec = getNextComboSeq(stack, comboSec, false, player);

		        	setComboSequence(tag, comboSec);

            		tag.setLong(lastActionTimeStr, player.worldObj.getTotalWorldTime());

	            }
	        }
		}
		//無敵時間無視
		entity.hurtResistantTime = 0;


		return false;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack sitem, World par2World,
			EntityPlayer par3EntityPlayer) {

		return super.onItemRightClick(sitem, par2World, par3EntityPlayer);
	}

    public void procSlashDim(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer){

        NBTTagCompound tag = getItemTagCompound(par1ItemStack);

        float distance = 30.0f;
        Entity target = null;

        int eId = tag.getInteger(TargetEntityStr);

        if(eId != 0){
            Entity tmp = par2World.getEntityByID(eId);
            if(tmp != null){
                if(tmp.getDistanceToEntity(par3EntityPlayer) < 30.0f)
                    target = tmp;
            }
        }

        if(target == null)
            for(int dist = 2; dist < 20; dist+=2){
                AxisAlignedBB bb = par3EntityPlayer.boundingBox.copy();
                Vec3 vec = par3EntityPlayer.getLookVec();
                vec = vec.normalize();
                bb = bb.expand(2.0f, 0.25f, 2.0f);
                bb = bb.offset(vec.xCoord*(float)dist,vec.yCoord*(float)dist,vec.zCoord*(float)dist);

                List<Entity> list = par2World.getEntitiesWithinAABBExcludingEntity(par3EntityPlayer, bb, AttackableSelector);
                for(Entity curEntity : list){
                    float curDist = curEntity.getDistanceToEntity(par3EntityPlayer);
                    if(curDist < distance)
                    {
                        target = curEntity;
                        distance = curDist;
                    }
                }
                if(target != null)
                    break;
            }

        setComboSequence(tag,ComboSequence.SlashDim);
        if(target != null){

            int soul = tag.getInteger(proudSoulStr);

            final int sdCost = 20;

            if(sdCost <= soul){
                soul -= sdCost;
                soul = Math.max(0,Math.min(999999999, soul));
                tag.setInteger(proudSoulStr, soul);
            }else{
                damageItem(10, par1ItemStack, par3EntityPlayer);
            }

            //target.spawnExplosionParticle();
            par2World.spawnParticle("largeexplode",
                    target.posX ,
                    target.posY + target.height,
                    target.posZ ,
                    3.0, 3.0, 3.0);
            par2World.spawnParticle("largeexplode",
                    target.posX + 1.0 ,
                    target.posY + target.height +1.0,
                    target.posZ ,
                    3.0, 3.0, 3.0);
            par2World.spawnParticle("largeexplode",
                    target.posX  ,
                    target.posY + target.height +0.5,
                    target.posZ + 1.0,
                    3.0, 3.0, 3.0);

            AxisAlignedBB bb = target.boundingBox.copy();
            bb = bb.expand(2.0f, 0.25f, 2.0f);


            tag.setBoolean(onClickStr, true);
            List<Entity> list = par2World.getEntitiesWithinAABBExcludingEntity(par3EntityPlayer, bb, AttackableSelector);

            if(!AttackableSelector.isEntityApplicable(target))
                list.add(target);

            int level = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, par1ItemStack);
            float magicDamage = 0 < level ? 1.0f + (float)(tag.getFloat(attackAmplifierStr) * (level / 5.0)) : 0;
            for(Entity curEntity : list){
                par3EntityPlayer.attackTargetEntityWithCurrentItem(curEntity);
                par3EntityPlayer.onCriticalHit(curEntity);

                if(0.0 < magicDamage){
                    curEntity.hurtResistantTime = 0;
                    DamageSource ds = new EntityDamageSource("directMagic",par3EntityPlayer).setDamageBypassesArmor().setMagicDamage();
                    curEntity.attackEntityFrom(ds, magicDamage);
                }
            }
            tag.setBoolean(onClickStr, false);

        }
    }

    public void procDrive(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer, ComboSequence setCombo){

        NBTTagCompound tag = getItemTagCompound(par1ItemStack);

        if(!par2World.isRemote){

            int soul = tag.getInteger(proudSoulStr);

            final int sdCost = 10;

            if(sdCost <= soul){
                soul -= sdCost;
                soul = Math.max(0,Math.min(999999999, soul));
                tag.setInteger(proudSoulStr, soul);
            }else{
                damageItem(5, par1ItemStack, par3EntityPlayer);
            }

            int level = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, par1ItemStack);
            float magicDamage = 0 < level ? 1.0f + (float)(tag.getFloat(attackAmplifierStr) * (level / 5.0)) : 0;

            EntityDrive entityDrive = new EntityDrive(par2World, par3EntityPlayer, magicDamage,false,90.0f - setCombo.swingDirection);
            if (entityDrive != null) {
                par2World.spawnEntityInWorld(entityDrive);
            }

            setComboSequence(tag, setCombo);
            return;
        }
    }


    public void procRindou(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer){

        NBTTagCompound tag = getItemTagCompound(par1ItemStack);

        if(!par2World.isRemote){

            int soul = tag.getInteger(proudSoulStr);

            final int sdCost = 20;

            if(sdCost <= soul){
                soul -= sdCost;
                soul = Math.max(0,Math.min(999999999, soul));
                tag.setInteger(proudSoulStr, soul);
            }else{
                damageItem(10, par1ItemStack, par3EntityPlayer);
            }

            int level = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, par1ItemStack);
            float magicDamage = 0 < level ? 1.0f + (float)(tag.getFloat(attackAmplifierStr) * (level / 10.0)) : 0;

            final float[] speeds = {0.25f,0.3f,0.35f};
            for(int i = 0; i < speeds.length;i++){
                EntityDrive entityDrive = new EntityDrive(par2World, par3EntityPlayer, magicDamage,false,0);
                entityDrive.setInitialSpeed(speeds[i]);
                if (entityDrive != null) {
                    par2World.spawnEntityInWorld(entityDrive);
                }
            }
            {
                EntityDrive entityDrive = new EntityDrive(par2World, par3EntityPlayer, magicDamage,true,0);
                entityDrive.setInitialSpeed(0.225f);
                if (entityDrive != null) {
                    par2World.spawnEntityInWorld(entityDrive);
                }
            }


            setComboSequence(tag, ComboSequence.Kiriage);
            return;
        }
    }


    public void procChargeAttack(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer){

        NBTTagCompound tag = getItemTagCompound(par1ItemStack);
        int sat = tag.getInteger(SpecialAttackTypeStr);

        switch (sat){
            case 1:
                procDrive(par1ItemStack, par2World, par3EntityPlayer, ComboSequence.Kiriage);
                break;

            case 2:
                procRindou(par1ItemStack,par2World,par3EntityPlayer);
                break;

            default:
                procSlashDim(par1ItemStack,par2World,par3EntityPlayer);
                break;
        }

        tag.setBoolean(isChargedStr,true);

    }

	@Override
	public void onPlayerStoppedUsing(ItemStack par1ItemStack, World par2World,
			EntityPlayer par3EntityPlayer, int par4) {

		NBTTagCompound tag = getItemTagCompound(par1ItemStack);


		int var6 = this.getMaxItemUseDuration(par1ItemStack) - par4;

		EnumSet<SwordType> swordType = getSwordType(par1ItemStack);

		if(RequiredChargeTick < var6 && swordType.contains(SwordType.Enchanted) && !swordType.contains(SwordType.Broken)){

			par3EntityPlayer.swingItem();

            procChargeAttack(par1ItemStack, par2World, par3EntityPlayer);

    		tag.setLong(lastActionTimeStr, par3EntityPlayer.worldObj.getTotalWorldTime());


		}else{
			tag.setBoolean(onClickStr, true);
		}

	}

    public NBTTagCompound getAttrTag(String attrName ,AttributeModifier par0AttributeModifier)
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        nbttagcompound.setString("AttributeName",attrName);
        nbttagcompound.setString("Name", par0AttributeModifier.getName());
        nbttagcompound.setDouble("Amount", par0AttributeModifier.getAmount());
        nbttagcompound.setInteger("Operation", par0AttributeModifier.getOperation());
        nbttagcompound.setLong("UUIDMost", par0AttributeModifier.getID().getMostSignificantBits());
        nbttagcompound.setLong("UUIDLeast", par0AttributeModifier.getID().getLeastSignificantBits());
        return nbttagcompound;
    }

    public AxisAlignedBB getBBofCombo(ItemStack itemStack, ComboSequence combo, EntityLivingBase user){

    	NBTTagCompound tag = getItemTagCompound(itemStack);
    	EnumSet<SwordType> swordType = getSwordType(itemStack);

    	AxisAlignedBB bb = user.boundingBox.copy();

    	Vec3 vec = user.getLookVec();
    	vec.yCoord = 0;
    	vec = vec.normalize();

    	switch (combo) {
		case Battou:
			if(swordType.contains(SwordType.Broken)){
				bb = bb.expand(1.0f, 0.0f, 1.0f);
				bb = bb.offset(vec.xCoord*1.0f,0,vec.zCoord*1.0f);

			}else if(swordType.containsAll(SwordType.BewitchedPerfect)){
				bb = bb.expand(5.0f, 0.25f, 5.0f);
			}else{
				bb = bb.expand(2.0f, 0.25f, 2.0f);
				bb = bb.offset(vec.xCoord*2.5f,0,vec.zCoord*2.5f);
			}
			break;

		case Iai:
			bb = bb.expand(2.0f, 0.25f, 2.0f);
			bb = bb.offset(vec.xCoord*2.5f,0,vec.zCoord*2.5f);
			break;

		case Saya1:
		case Saya2:
			bb = bb.expand(1.2f, 0.25f, 1.2f);
			bb = bb.offset(vec.xCoord*2.0f,0,vec.zCoord*2.0f);
			break;

		case Kiriorosi:
		default:
			bb = bb.expand(1.2f, 1.25f, 1.2f);
			bb = bb.offset(vec.xCoord*2.0f,0.5f,vec.zCoord*2.0f);
			break;
		}

    	return bb;
    }

    public enum SwordType{
    	Broken,
    	Perfect,
    	Enchanted,
    	Bewitched,
    	SoulEeater,
    	FiercerEdge,
        NoScabbard,
        Sealed,
    	;

    	public static final EnumSet<SwordType> BewitchedSoulEater = EnumSet.of(SwordType.SoulEeater,SwordType.Bewitched);
    	public static final EnumSet<SwordType> BewitchedPerfect = EnumSet.of(SwordType.Perfect,SwordType.Bewitched);
    }

    public EnumSet<SwordType> getSwordType(ItemStack itemStack){
    	EnumSet<SwordType> result = EnumSet.noneOf(SwordType.class);

		NBTTagCompound tag = getItemTagCompound(itemStack);

		if(itemStack.getItemDamage() == 0)
			result.add(SwordType.Perfect);

		if(tag.getBoolean(isBrokenStr)){
			if(result.contains(SwordType.Perfect)){
				tag.setBoolean(isBrokenStr, false);
			}else{
				result.add(SwordType.Broken);
			}
		}

        if(tag.getBoolean(isSealedStr)){
            result.add(SwordType.Sealed);
        }else{
            if(itemStack.isItemEnchanted()){
                result.add(SwordType.Enchanted);

                if(itemStack.hasDisplayName()){
                    result.add(SwordType.Bewitched);
                }
            }
        }

    	if(1000 < tag.getInteger(proudSoulStr))
    		result.add(SwordType.SoulEeater);

    	if(1000 < tag.getInteger(killCountStr))
    		result.add(SwordType.FiercerEdge);

        if(tag.getBoolean(isNoScabbardStr)){
            result.add(SwordType.NoScabbard);
        }

    	return result;
    }

    public void updateAttackAmplifier(EnumSet<SwordType> swordType,NBTTagCompound tag,EntityPlayer el,ItemStack sitem){
    	float tagAttackAmplifier = tag.getFloat(attackAmplifierStr);

		float attackAmplifier = 0;

		if(swordType.contains(SwordType.Broken)){
        	attackAmplifier = -4;
		}else if(swordType.contains(SwordType.FiercerEdge)){
        	float tmp = el.experienceLevel;
        	tmp = 1.0f + (float)( tmp < 15.0f ? tmp * 0.5f : tmp < 30.0f ? 3.0f +tmp*0.45f : 7.0f+0.4f * tmp);
        	attackAmplifier = tmp;
		}

        if(tagAttackAmplifier != attackAmplifier)
        {
        	tag.setFloat(attackAmplifierStr, attackAmplifier);

        	NBTTagList attrTag = null;

    		attrTag = new NBTTagList();
    		tag.setTag("AttributeModifiers",attrTag);

        	attrTag.appendTag(
        			getAttrTag(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(),new AttributeModifier(field_111210_e, "Weapon modifier", (double)(attackAmplifier + baseAttackModifiers), 0))
        			);

        	el.getAttributeMap().removeAttributeModifiers(sitem.getAttributeModifiers());
        	el.getAttributeMap().applyAttributeModifiers(sitem.getAttributeModifiers());
        }
    }


	@Override
	public void onUpdate(ItemStack sitem, World par2World,
			Entity par3Entity, int indexOfMainSlot, boolean isCurrent) {

		if(!(par3Entity instanceof EntityPlayer)){
			super.onUpdate(sitem, par2World, par3Entity, indexOfMainSlot, isCurrent);
			return;
		}

		EntityPlayer el = (EntityPlayer)par3Entity;

		NBTTagCompound tag = getItemTagCompound(sitem);

		int curDamage = sitem.getItemDamage();

		EnumSet<SwordType> swordType = getSwordType(sitem);

		updateAttackAmplifier(swordType, tag ,el, sitem);


		{
			int cost = sitem.getRepairCost();
			if(cost != 0){
				Map map = EnchantmentHelper.getEnchantments(sitem);

				cost = map.size() + 1;
				cost *= AnvilRepairBonus;

				int soul = tag.getInteger(proudSoulStr);
				soul += cost;
				soul = Math.max(0,Math.min(999999999, soul));
				tag.setInteger(proudSoulStr, soul);

                int cnt = tag.getInteger(RepairCounterStr);
                cnt += 1;
                cnt = Math.min(999999999, cnt);
                tag.setInteger(RepairCounterStr,cnt);

				sitem.setRepairCost(0);
			}
		}

        if(!par2World.isRemote && isCurrent && par2World.getTotalWorldTime() % 20 == 0){
        	int nowExp = el.experienceTotal;

        	final String prevExpStr = "prevExp";

        	if(!tag.hasKey(prevExpStr)){
        		tag.setInteger(prevExpStr, nowExp);
        	}

        	int prevExp = tag.getInteger(prevExpStr);

        	int repair = nowExp - prevExp;


        	if(0 < repair){
            	if(0 < curDamage && swordType.containsAll(SwordType.BewitchedSoulEater) && !swordType.contains(SwordType.NoScabbard)){
                	if(10 < repair ){
                		repair = 11;
                	}
            		sitem.setItemDamage(Math.max(0,curDamage-repair));

            	}else{
        			int soul = tag.getInteger(proudSoulStr);

    				soul += repair;
    				soul = Math.max(0,Math.min(999999999, soul));
    				tag.setInteger(proudSoulStr, soul);
            	}
        	}


    		tag.setInteger(prevExpStr, el.experienceTotal);
        }

		if(!isCurrent && !par2World.isRemote){
			if(swordType.contains(SwordType.Bewitched) && !swordType.contains(SwordType.NoScabbard) && 0 < curDamage && par2World.getTotalWorldTime() % 20 == 0){

				int idx = Arrays.asList(el.inventory.mainInventory).indexOf(sitem);

				if(0<= idx && idx < 9 && 0 < el.experienceLevel){
					int repair;
					int descExp;

					if(swordType.contains(SwordType.Broken)){
						el.addExhaustion(0.025F);
						repair = 10;
						descExp = 5;
					}else{
						repair = 1;
						descExp = 1;
						el.addExhaustion(0.025F);
					}

					if(0 < curDamage){
						sitem.setItemDamage(Math.max(0,curDamage-repair));
					}

					int soul = tag.getInteger(proudSoulStr);

    				soul += descExp;
    				soul = Math.max(0,Math.min(999999999, soul));
    				tag.setInteger(proudSoulStr, soul);

					for(;descExp > 0;descExp--){
						el.addExperience(-1);

						if(el.experience < 0){
							if(el.experienceLevel <= 0){
								el.experience = 0;
							}else{
								el.experienceLevel--;
								el.experience = 1.0f - (0.9f/el.xpBarCap());
							}
						}
					}
				}
			}
		}

		if(el.onGround && !el.isAirBorne && tag.getBoolean(onJumpAttackedStr)){
			setComboSequence(tag, ComboSequence.None);
		}

		if(el.onGround && tag.getBoolean(onJumpAttackedStr))
			tag.setBoolean(onJumpAttackedStr, false);


		ComboSequence comboSeq = getComboSequence(tag);

		long prevAttackTime = tag.getLong(lastActionTimeStr);
		long currentTime =par2World.getTotalWorldTime();

		if(isCurrent){

			if(tag.getBoolean(onClickStr)){

				//sitem.setItemDamage(1320);
				if(prevAttackTime + ComboInterval < currentTime){

					comboSeq = getNextComboSeq(sitem, comboSeq, true, el);
					setComboSequence(tag, comboSeq);

                    //if(el.worldObj.isRemote)
    				el.isSwingInProgress = true;
					onEntitySwing(el,sitem);

					AxisAlignedBB bb = getBBofCombo(sitem, comboSeq, el);

					List<Entity> list = par2World.getEntitiesWithinAABBExcludingEntity(el, bb, AttackableSelector);
					for(Entity curEntity : list){

						switch (comboSeq) {
						case Saya1:
						case Saya2:
							float attack = 4.0f + Item.ToolMaterial.STONE.getDamageVsEntity(); //stone like
							if(swordType.contains(SwordType.Broken))
								attack = Item.ToolMaterial.EMERALD.getDamageVsEntity();
							else if(swordType.contains(SwordType.FiercerEdge) && el instanceof EntityPlayer)
			                	attack += tag.getFloat(attackAmplifierStr) * 0.5f;

							if (curEntity instanceof EntityLivingBase)
			                {
				                float var4 = 0;
			                    var4 = EnchantmentHelper.getEnchantmentModifierLiving(el, (EntityLiving)curEntity);
				                if(var4 > 0)
				                	attack += var4;
			                }


			                if (curEntity instanceof EntityLivingBase){
			                	attack = Math.min(attack,((EntityLivingBase)curEntity).getHealth()-1);
			                }


							curEntity.hurtResistantTime = 0;
							curEntity.attackEntityFrom(DamageSource.causeMobDamage(el), attack);


			                if (curEntity instanceof EntityLivingBase){
			                	this.hitEntity(sitem, (EntityLivingBase)curEntity, el);
			                }

							break;

                        case None:
                            break;

						default:
							((EntityPlayer)el).attackTargetEntityWithCurrentItem(curEntity);
							((EntityPlayer)el).onCriticalHit(curEntity);
							break;
						}
					}
					tag.setBoolean(onClickStr, false);

					tag.setLong(lastActionTimeStr, currentTime);

					if(swordType.containsAll(SwordType.BewitchedPerfect) && comboSeq.equals(ComboSequence.Battou)){
						damageItem(10, sitem, el);
					}
				}
			}else{
				if(((prevAttackTime + comboSeq.comboResetTicks) < currentTime)
						&& (comboSeq.useScabbard
					       || el.swingProgressInt == 0)
					    && (!el.isUsingItem())
						){
					switch (comboSeq) {
					case None:
						break;

					case Noutou:
						//※動かず納刀完了させ、敵に囲まれている場合にボーナス付与。

						if(tag.getInteger(lastPosHashStr) == (int)((el.posX + el.posY + el.posZ) * 10.0)){

							AxisAlignedBB bb = el.boundingBox.copy();
							bb = bb.expand(10, 5, 10);
							List<Entity> list = par2World.getEntitiesWithinAABBExcludingEntity(el, bb, AttackableSelector);

							if(0 < list.size()){
								if(swordType.containsAll(SwordType.BewitchedSoulEater)
										&& 10 < sitem.getItemDamage()){
									int j1 = (int)Math.min(Math.ceil(list.size() * 0.5),5);
							        dropXpOnBlockBreak(par2World, MathHelper.ceiling_double_int(el.posX), MathHelper.ceiling_double_int(el.posY), MathHelper.ceiling_double_int(el.posZ), j1);
								}

								el.onCriticalHit(el);
								if(!el.worldObj.isRemote){
									el.addPotionEffect(new PotionEffect(Potion.damageBoost.getId(),200,3,true));
									el.addPotionEffect(new PotionEffect(Potion.resistance.getId(),200,3,true));
								}
							}

						}


					case SlashDim:
					case Iai:
							setComboSequence(tag, ComboSequence.None);
							break;
					default:
						if(comboSeq.useScabbard){
							setComboSequence(tag, ComboSequence.None);
							break;
						}
						setComboSequence(tag, ComboSequence.Noutou);


						tag.setInteger(lastPosHashStr,(int)((el.posX + el.posY + el.posZ) * 10.0));
						tag.setLong(lastActionTimeStr, currentTime);
						el.swingItem();
						break;
					}
				}

				if(!comboSeq.equals(ComboSequence.None) && el.swingProgressInt != 0 && currentTime < (prevAttackTime + comboSeq.comboResetTicks)){
					onEntitySwing(el,sitem);
				}
			}



			if(swordType.contains(SwordType.Bewitched)){
				AxisAlignedBB bb = el.boundingBox.copy();
				bb = bb.expand(1, 1, 1);
				List<Entity> list = par2World.getEntitiesWithinAABBExcludingEntity(el, bb, this.AttackableSelector);
				if(0 < list.size() && el.isAirBorne){
					Entity target = null;
					float distance = 10.0f;
					for(Entity curEntity : list){
						float curDist = curEntity.getDistanceToEntity(el);
						if(curDist < distance)
						{
							target = curEntity;
							distance = curDist;
						}
					}

					if(target != null){
						el.onGround = true;
						el.setJumping(false);
					}
				}
			}
		}else{
			if(!comboSeq.equals(ComboSequence.None) && ((prevAttackTime + comboSeq.comboResetTicks) < currentTime)){
				setComboSequence(tag, ComboSequence.None);
			}
		}



		if(sitem.equals(el.getHeldItem())){

            if(!el.worldObj.isRemote){
                int eId = tag.getInteger(TargetEntityStr);

                if(el.isSneaking()){
                    if(eId == 0){



                        Entity rayEntity = getRayTrace(el,10.0f);

                        if(rayEntity !=null){
                            if(!AttackableSelector.isEntityApplicable(rayEntity)){

                            }
                        }


                        if(rayEntity != null){
                            eId = rayEntity.getEntityId();

                        }else{
                            AxisAlignedBB bb = el.boundingBox.copy();
                            bb = bb.expand(10, 5, 10);
                            float distance = 20.0f;

                            List<Entity> list = par2World.getEntitiesWithinAABBExcludingEntity(el, bb, AttackableSelector);
                            for(Entity curEntity : list){
                                float curDist = curEntity.getDistanceToEntity(el);
                                if(curDist < distance)
                                {
                                    eId = curEntity.getEntityId();
                                    distance = curDist;
                                }
                            }
                        }
                        tag.setInteger(TargetEntityStr, eId);
                    }else{

                        if(3 <= EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId, sitem)){
                            Entity target = par2World.getEntityByID(eId);
                            if(target != null && target instanceof EntityWither
                                    && 10 > el.getDistanceToEntity(target)
                                    && ((EntityWither)target).getHealth() / ((EntityWither)target).getMaxHealth() > 0.5)
                            {


                                Vec3 vec = el.getLookVec();

                                double y = -vec.yCoord * 2.0;
                                if(target.posY <= el.posY + 5.0)
                                    y = 0;

                                target.addVelocity(vec.xCoord,y,vec.zCoord);
                            }
                        }
                    }
                    /*
                    Entity target = par2World.getEntityByID(eId);
                    if(target != null)
                        this.faceEntity(el,target, 1000.0f,1000.0f);
*/
                }else if(eId != 0){
                    tag.setInteger(TargetEntityStr, 0);
                }
            }else{

                int eId = tag.getInteger(TargetEntityStr);
                if(eId != 0){
                    Entity target = par2World.getEntityByID(eId);
                    if(target != null)
                        this.faceEntity(el,target, 1000.0f,1000.0f);
                }
            }


		}
	}


    protected void dropXpOnBlockBreak(World par1World, int par2, int par3, int par4, int par5)
    {
        if (!par1World.isRemote)
        {
            while (par5 > 0)
            {
                int i1 = EntityXPOrb.getXPSplit(par5);
                par5 -= i1;
                par1World.spawnEntityInWorld(new EntityXPOrb(par1World, (double)par2 + 0.5D, (double)par3 + 0.5D, (double)par4 + 0.5D, i1));
            }
        }
    }

    public void faceEntity(EntityLivingBase owner, Entity par1Entity, float par2, float par3)
    {
        double d0 = par1Entity.posX - owner.posX;
        double d1 = par1Entity.posZ - owner.posZ;
        double d2;

        if (par1Entity instanceof EntityLivingBase)
        {
            EntityLivingBase entitylivingbase = (EntityLivingBase)par1Entity;
            d2 = entitylivingbase.posY + (double)entitylivingbase.getEyeHeight() - (owner.posY + (double)owner.getEyeHeight());
        }
        else
        {
            d2 = (par1Entity.boundingBox.minY + par1Entity.boundingBox.maxY) / 2.0D - (owner.posY + (double)owner.getEyeHeight());
        }

        double d3 = (double)MathHelper.sqrt_double(d0 * d0 + d1 * d1);
        float f2 = (float)(Math.atan2(d1, d0) * 180.0D / Math.PI) - 90.0F;
        float f3 = (float)(-(Math.atan2(d2, d3) * 180.0D / Math.PI));


        owner.rotationPitch = this.updateRotation(owner.rotationPitch, f3, par3);
        owner.rotationPitch = (float)Math.min(Math.max(owner.rotationPitch,-30), 60);

        owner.rotationYaw = this.updateRotation(owner.rotationYaw, f2, par2);
    }

    private float updateRotation(float par1, float par2, float par3)
    {
        float f3 = MathHelper.wrapAngleTo180_float(par2 - par1);

        if (f3 > par3)
        {
            f3 = par3;
        }

        if (f3 < -par3)
        {
            f3 = -par3;
        }

        return par1 + f3;
    }
    
    public void addInformationSwordClass(ItemStack par1ItemStack,
			EntityPlayer par2EntityPlayer, List par3List, boolean par4) {

		EnumSet<SwordType> swordType = getSwordType(par1ItemStack);
		if(swordType.contains(SwordType.Enchanted)){
			if(swordType.contains(SwordType.Bewitched)){
				par3List.add(String.format("§5%s", StatCollector.translateToLocal("flammpfeil.swaepon.info.bewitched")));
			}else{
				par3List.add(String.format("§3%s", StatCollector.translateToLocal("flammpfeil.swaepon.info.magic")));
			}
		}else{
			par3List.add(String.format("§8%s", StatCollector.translateToLocal("flammpfeil.swaepon.info.noname")));
		}
    }

    public void addInformationKillCount(ItemStack par1ItemStack,
    		EntityPlayer par2EntityPlayer, List par3List, boolean par4) {
    	EnumSet<SwordType> swordType = getSwordType(par1ItemStack);
		NBTTagCompound tag = getItemTagCompound(par1ItemStack);

		par3List.add(String.format("%sKillCount : %d", swordType.contains(SwordType.FiercerEdge) ? "§4" : "", tag.getInteger(killCountStr)));

    }

    public void addInformationProudSoul(ItemStack par1ItemStack,
                                        EntityPlayer par2EntityPlayer, List par3List, boolean par4) {
        EnumSet<SwordType> swordType = getSwordType(par1ItemStack);
        NBTTagCompound tag = getItemTagCompound(par1ItemStack);

        par3List.add(String.format("%sProudSoul : %d", swordType.contains(SwordType.SoulEeater)  ? "§5" : "", tag.getInteger(proudSoulStr)));

    }

    public void addInformationSpecialAttack(ItemStack par1ItemStack,
                                        EntityPlayer par2EntityPlayer, List par3List, boolean par4) {
        EnumSet<SwordType> swordType = getSwordType(par1ItemStack);

        if(swordType.contains(SwordType.Bewitched)){
            NBTTagCompound tag = getItemTagCompound(par1ItemStack);
            int sat = tag.getInteger(SpecialAttackTypeStr);

            final String[] keys = {
                    "flammpfeil.slashblade.specialattack.slashdimension",
                    "flammpfeil.slashblade.specialattack.drive",
                    "flammpfeil.slashblade.specialattack.rindou"
                };

            String key = (0 <= sat && sat < keys.length) ? keys[sat] : keys[0];


            par3List.add(String.format("SA:%s",  StatCollector.translateToLocal(key)));
        }
    }
    
	@Override
	public void addInformation(ItemStack par1ItemStack,
			EntityPlayer par2EntityPlayer, List par3List, boolean par4) {


		super.addInformation(par1ItemStack, par2EntityPlayer, par3List, par4);

		addInformationSwordClass(par1ItemStack, par2EntityPlayer, par3List, par4);

		addInformationKillCount(par1ItemStack, par2EntityPlayer, par3List, par4);

		addInformationProudSoul(par1ItemStack, par2EntityPlayer, par3List, par4);

        addInformationSpecialAttack(par1ItemStack, par2EntityPlayer, par3List, par4);

		NBTTagCompound tag = getItemTagCompound(par1ItemStack);
        if(tag.hasKey(adjustXStr)){
            float ax = tag.getFloat(adjustXStr);
            float ay = tag.getFloat(adjustYStr);
            float az = tag.getFloat(adjustZStr);
            par3List.add(String.format("adjust x:%.1f y:%.1f z:%.1f", ax,ay,az));
        }
	}


    public Vec3 getEntityToEntityVec(Entity root, Entity target, float yawLimit, float pitchLimit)
    {
        double d0 = (target.posX + target.motionX) - root.posX;
        double d1 = (target.posZ + target.motionZ) - root.posZ;
        double d2;

        if (target instanceof EntityLivingBase)
        {
            EntityLivingBase entitylivingbase = (EntityLivingBase)target;
            d2 = entitylivingbase.posY + entitylivingbase.motionY + (double)entitylivingbase.getEyeHeight() - (root.posY + (double)root.getEyeHeight());
        }
        else
        {
            d2 = (target.boundingBox.minY+ target.boundingBox.maxY) / 2.0D  + target.motionY  - (root.posY + (double)root.getEyeHeight());
        }

        double d3 = (double)MathHelper.sqrt_double(d0 * d0 + d1 * d1);
        float f2 = (float)(Math.atan2(d1, d0) * 180.0D / Math.PI) - 90.0F;
        float f3 = (float)(-(Math.atan2(d2, d3) * 180.0D / Math.PI));


        double x,y,z;

        double yaw = Math.atan2(d1, d0) - Math.PI / 2.0f;
        double pitch = Math.atan2(d2, d3);

        y = Math.sin(pitch);
        x = -Math.sin(yaw);
        z = Math.cos(yaw);

        return Vec3.createVectorHelper(x, y, z).normalize();
    }

	public void ReflectionProjecTile(Entity projecTile,EntityLivingBase player){

		Entity target = null;

    	if(projecTile instanceof EntityFireball)
    		target = ((EntityFireball)projecTile).shootingEntity;
    	else if(projecTile instanceof EntityArrow)
    		target = ((EntityArrow)projecTile).shootingEntity;


    	if(target != null){
    		Vec3 vec = this.getEntityToEntityVec(projecTile,target,360.0f,360.0f);
			InductionProjecTile(projecTile,player,vec);
    	}else{
    		Vec3 vec = Vec3.createVectorHelper(-projecTile.motionX,-projecTile.motionY,-projecTile.motionZ);
    		vec = vec.normalize();
			InductionProjecTile(projecTile,player,vec);
//    		InductionProjecTile(projecTile,player);
    	}

	}


	public void InductionProjecTile(Entity projecTile,EntityLivingBase user){
		InductionProjecTile(projecTile,user,user.getLookVec());
	}
	public void InductionProjecTile(Entity projecTile,EntityLivingBase user,Vec3 dir){

        if (dir != null)
        {
        	//projecTile.velocityChanged = true;

        	Vec3 vector = Vec3.createVectorHelper(projecTile.motionX,projecTile.motionY,projecTile.motionZ);

        	projecTile.motionX = dir.xCoord;
        	projecTile.motionY = dir.yCoord;
        	projecTile.motionZ = dir.zCoord;

        	if(projecTile instanceof EntityFireball){
	        	((EntityFireball)projecTile).accelerationX = projecTile.motionX * 0.1D;
	        	((EntityFireball)projecTile).accelerationY = projecTile.motionY * 0.1D;
	        	((EntityFireball)projecTile).accelerationZ = projecTile.motionZ * 0.1D;
        	}

        	if(projecTile instanceof EntityArrow){
        		((EntityArrow)projecTile).setIsCritical(true);
        	}

        	/*
        	if(projecTile instanceof EntityThrowable){
        	}
        	/**/

        	/*
			if(projecTile instanceof IThrowableEntity){
        	}
        	/**/

        	projecTile.motionX *= 1.5;
        	projecTile.motionY *= 1.5;
        	projecTile.motionZ *= 1.5;

        }

        if (user != null)
        {
        	if(projecTile instanceof EntityFireball)
        		((EntityFireball)projecTile).shootingEntity = user;
        	else if(projecTile instanceof EntityArrow){
        		((EntityArrow)projecTile).shootingEntity = user;
        	}else if(projecTile instanceof IThrowableEntity)
        		((IThrowableEntity)projecTile).setThrower(user);
        	else if(projecTile instanceof EntityThrowable){
        		if(user instanceof EntityPlayer){
            		NBTTagCompound tag = new NBTTagCompound();
            		((EntityThrowable)projecTile).writeEntityToNBT(tag);
            		tag.setString("ownerName", ((EntityPlayer)user).getCommandSenderName());
            		((EntityThrowable)projecTile).readEntityFromNBT(tag);
        		}
        	}
        }
	}

	@Override
	public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack) {

		ComboSequence comboSeq = getComboSequence(getItemTagCompound(stack));

		if(!comboSeq.equals(ComboSequence.None))
		{
			int destructedCount = 0;

			AxisAlignedBB bb = getBBofCombo(
					stack,
					comboSeq,
					entityLiving);
			List<Entity> list = entityLiving.worldObj.getEntitiesWithinAABBExcludingEntity(entityLiving, bb,DestructableSelector);
			for(Entity curEntity : list){

				boolean isDestruction = true;

				EnumSet<SwordType> swordType =getSwordType(stack);

				if(curEntity instanceof EntityFireball){
					if((((EntityFireball)curEntity).shootingEntity != null && ((EntityFireball)curEntity).shootingEntity.getEntityId() == entityLiving.getEntityId())){
						isDestruction = false;
					}else if(!swordType.contains(SwordType.Bewitched)){
						isDestruction = !curEntity.attackEntityFrom(DamageSource.causeMobDamage(entityLiving),this.baseAttackModifiers);
					}

					if(isDestruction && swordType.contains(SwordType.Bewitched)){
						if(0 < EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId, stack)){
							ReflectionProjecTile(curEntity,entityLiving);
						}else{
							InductionProjecTile(curEntity,entityLiving);
						}
						isDestruction = false;
					}

				}else if(curEntity instanceof EntityArrow){
					if((((EntityArrow)curEntity).shootingEntity != null && ((EntityArrow)curEntity).shootingEntity.getEntityId() == entityLiving.getEntityId())){
						isDestruction = false;
					}

					if(isDestruction && swordType.contains(SwordType.Bewitched)){
						if(0 < EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId, stack)){
							ReflectionProjecTile(curEntity,entityLiving);
						}else{
							Entity target = null;

							NBTTagCompound tag = stack.getTagCompound();
							int eId = tag.getInteger(TargetEntityStr);
				            if(eId != 0){
				                Entity tmp = entityLiving.worldObj.getEntityByID(eId);
				                if(tmp != null){
				                    if(tmp.getDistanceToEntity(entityLiving) < 30.0f)
				                        target = tmp;
				                }
				            }
							if(target != null && target instanceof EntityCreeper){
								InductionProjecTile(curEntity, null, entityLiving.getLookVec());
							}else{
								InductionProjecTile(curEntity, entityLiving);
							}
						}
						isDestruction = false;
					}
				}else if(curEntity instanceof IThrowableEntity){
					if((((IThrowableEntity)curEntity).getThrower() != null && ((IThrowableEntity)curEntity).getThrower().getEntityId() == entityLiving.getEntityId())){
						isDestruction = false;
					}

					if(isDestruction && swordType.contains(SwordType.Bewitched)){
						if(0 < EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId, stack)){
							ReflectionProjecTile(curEntity,entityLiving);
						}else{
							InductionProjecTile(curEntity,entityLiving);
						}
						isDestruction = false;
					}
				}else if(curEntity instanceof EntityThrowable){
					if((((EntityThrowable)curEntity).getThrower() != null && ((EntityThrowable)curEntity).getThrower().getEntityId() == entityLiving.getEntityId())){
						isDestruction = false;
					}

					if(isDestruction && swordType.contains(SwordType.Bewitched)){
						if(0 < EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId, stack)){
							ReflectionProjecTile(curEntity,entityLiving);
						}else{
							InductionProjecTile(curEntity,entityLiving);
						}
						isDestruction = false;
					}
				}

				if(!isDestruction)
					continue;
				else{
					curEntity.motionX = 0;
                    curEntity.motionY = 0;
                    curEntity.motionZ = 0;
					curEntity.setDead();

			        for (int var1 = 0; var1 < 10; ++var1)
			        {
			        	Random rand = entityLiving.getRNG();
			            double var2 = rand.nextGaussian() * 0.02D;
			            double var4 = rand.nextGaussian() * 0.02D;
			            double var6 = rand.nextGaussian() * 0.02D;
			            double var8 = 10.0D;
			            entityLiving.worldObj.spawnParticle("explode", curEntity.posX + (double)(rand.nextFloat() * curEntity.width * 2.0F) - (double)curEntity.width - var2 * var8, curEntity.posY + (double)(rand.nextFloat() * curEntity.height) - var4 * var8, curEntity.posZ + (double)(rand.nextFloat() * curEntity.width * 2.0F) - (double)curEntity.width - var6 * var8, var2, var4, var6);
			        }

					destructedCount++;
				}
			}

			if(0 < destructedCount){
				damageItem(1, stack, entityLiving);
			}
		}

		return super.onEntitySwing(entityLiving, stack);
	}


    public MovingObjectPosition rayTrace(EntityLivingBase owner, double par1, float par3)
    {
        Vec3 vec3 = getPosition(owner);
        Vec3 vec31 = owner.getLook(par3);
        Vec3 vec32 = vec3.addVector(vec31.xCoord * par1, vec31.yCoord * par1, vec31.zCoord * par1);
        return owner.worldObj.func_147447_a(vec3, vec32, false, false, true);
    }
    public Vec3 getPosition(EntityLivingBase owner)
    {
        return owner.worldObj.getWorldVec3Pool().getVecFromPool(owner.posX, owner.posY + owner.getEyeHeight(), owner.posZ);
    }

    public Entity getRayTrace(EntityLivingBase owner, double reachMax){
        Entity pointedEntity;
        float par1 = 1.0f;

        MovingObjectPosition objectMouseOver = rayTrace(owner, reachMax, par1);
        double reachMin = reachMax;
        Vec3 entityPos = getPosition(owner);

        if (objectMouseOver != null)
        {
            reachMin = objectMouseOver.hitVec.distanceTo(entityPos);
        }

        Vec3 lookVec = owner.getLook(par1);
        Vec3 reachVec = entityPos.addVector(lookVec.xCoord * reachMax, lookVec.yCoord * reachMax, lookVec.zCoord * reachMax);
        pointedEntity = null;
        float expandFactor = 1.0F;
        List<Entity> list = owner.worldObj.getEntitiesWithinAABBExcludingEntity(owner, owner.boundingBox.addCoord(lookVec.xCoord * reachMax, lookVec.yCoord * reachMax, lookVec.zCoord * reachMax).expand((double)expandFactor, (double)expandFactor, (double)expandFactor));
        double tmpDistance = reachMin;

        for(Entity entity : list){
            if (entity == null || !entity.canBeCollidedWith()) continue;

            float borderSize = entity.getCollisionBorderSize();
            AxisAlignedBB axisalignedbb = entity.boundingBox.expand((double)borderSize, (double)borderSize, (double)borderSize);
            MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(entityPos, reachVec);

            if (axisalignedbb.isVecInside(entityPos))
            {
                if (0.0D < tmpDistance || tmpDistance == 0.0D)
                {
                    pointedEntity = entity;
                    tmpDistance = 0.0D;
                }
            }
            else if (movingobjectposition != null)
            {
                double d3 = entityPos.distanceTo(movingobjectposition.hitVec);

                if (d3 < tmpDistance || tmpDistance == 0.0D)
                {
                    if (entity == owner.ridingEntity && !entity.canRiderInteract())
                    {
                        if (tmpDistance == 0.0D)
                        {
                            pointedEntity = entity;
                        }
                    }
                    else
                    {
                        pointedEntity = entity;
                        tmpDistance = d3;
                    }
                }
            }
        }

        return pointedEntity;
    }

    private String[] repairMaterialOreDic = null;
    public ItemSlashBlade setRepairMaterialOreDic(String... material){
    	this.repairMaterialOreDic = material;
    	return this;
    }

    private ItemStack repairMaterial = null;
    public ItemSlashBlade setRepairMaterial(ItemStack item){
    	this.repairMaterial = item;
    	return this;
    }
    @Override
    public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack)
    {
    	boolean result = false;
    	if(par2ItemStack.getItem() == SlashBlade.proudSoul){
    		result = true;
    	}

    	if(!result && this.repairMaterial != null)
    		result =par2ItemStack.isItemEqual(this.repairMaterial);

    	if(!result && this.repairMaterialOreDic != null)
    	{
    		for(String oreName : this.repairMaterialOreDic){
        		List<ItemStack> list = OreDictionary.getOres(oreName);
        		for(ItemStack curItem : list){
        			result = curItem.isItemEqual(par2ItemStack);
        			if(result)
        				break;
        		}
    		}
    	}
    	return result;

        //return this.toolMaterial.getToolCraftingMaterial() == par2ItemStack.itemID ? true : super.getIsRepairable(par1ItemStack, par2ItemStack);
    }

    @Override
    public void getSubItems(Item par1, CreativeTabs par2CreativeTabs,
    		List par3List) {
    	super.getSubItems(par1, par2CreativeTabs, par3List);

    	if(this == SlashBlade.weapon){
    		ItemStack item = new ItemStack(this);
    		item.setStackDisplayName(StatCollector.translateToLocal("item.flammpfeil.slashblade.yamato.name").trim());
    		item.addEnchantment(Enchantment.thorns, 1);
    		item.addEnchantment(Enchantment.featherFalling, 4);
    		item.addEnchantment(Enchantment.power, 5);
    		item.addEnchantment(Enchantment.punch, 2);
    		NBTTagCompound tag = getItemTagCompound(item);
			tag.setInteger(killCountStr, 1000);
			tag.setInteger(proudSoulStr, 1000);

            par3List.add(item.copy());

            tag.setBoolean(isNoScabbardStr,true);
            item.setStackDisplayName(item.getDisplayName() + "-NS");
            par3List.add(item.copy());

    		ItemStack itemBrokenWhite = GameRegistry.findItemStack(SlashBlade.modid, SlashBlade.BrokenBladeWhiteStr, 1);
            if(itemBrokenWhite != null) par3List.add(itemBrokenWhite);

    		ItemStack itemHundredKill = GameRegistry.findItemStack(SlashBlade.modid, SlashBlade.HundredKillSilverBambooLightStr, 1);
            if(itemHundredKill != null) par3List.add(itemHundredKill);
    	}
    }
}