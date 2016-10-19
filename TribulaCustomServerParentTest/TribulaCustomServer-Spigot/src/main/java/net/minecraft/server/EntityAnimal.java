package net.minecraft.server;

import javax.annotation.Nullable;

public abstract class EntityAnimal extends EntityAgeable implements IAnimal {

    protected Block bA;
    private int bx;
    private EntityHuman by;
    public ItemStack breedItem; // CraftBukkit - Add breedItem variable

    public EntityAnimal(World world) {
        super(world);
        this.bA = Blocks.GRASS;
    }

    protected void M() {
        if (this.getAge() != 0) {
            this.bx = 0;
        }

        super.M();
    }

    public void n() {
        super.n();
        if (this.getAge() != 0) {
            this.bx = 0;
        }

        if (this.bx > 0) {
            --this.bx;
            if (this.bx % 10 == 0) {
                double d0 = this.random.nextGaussian() * 0.02D;
                double d1 = this.random.nextGaussian() * 0.02D;
                double d2 = this.random.nextGaussian() * 0.02D;

                this.world.addParticle(EnumParticle.HEART, this.locX + (double) (this.random.nextFloat() * this.width * 2.0F) - (double) this.width, this.locY + 0.5D + (double) (this.random.nextFloat() * this.length), this.locZ + (double) (this.random.nextFloat() * this.width * 2.0F) - (double) this.width, d0, d1, d2);
            }
        }

    }

    /* CraftBukkit start
    // Function disabled as it has no special function anymore after
    // setSitting is disabled.
    public boolean damageEntity(DamageSource damagesource, float f) {
        if (this.isInvulnerable(damagesource)) {
            return false;
        } else {
            this.bx = 0;
            return super.damageEntity(damagesource, f);
        }
    }
    // CraftBukkit end */

    public float a(BlockPosition blockposition) {
        return this.world.getType(blockposition.down()).getBlock() == Blocks.GRASS ? 10.0F : this.world.n(blockposition) - 0.5F;
    }

    public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        nbttagcompound.setInt("InLove", this.bx);
    }

    public double ax() {
        return 0.29D;
    }

    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        this.bx = nbttagcompound.getInt("InLove");
    }

    public boolean cK() {
        int i = MathHelper.floor(this.locX);
        int j = MathHelper.floor(this.getBoundingBox().b);
        int k = MathHelper.floor(this.locZ);
        BlockPosition blockposition = new BlockPosition(i, j, k);

        return this.world.getType(blockposition.down()).getBlock() == this.bA && this.world.j(blockposition) > 8 && super.cK();
    }

    public int C() {
        return 120;
    }

    protected boolean isTypeNotPersistent() {
        return false;
    }

    protected int getExpValue(EntityHuman entityhuman) {
        return 1 + this.world.random.nextInt(3);
    }

    public boolean e(@Nullable ItemStack itemstack) {
        return itemstack != null && itemstack.getItem() == Items.WHEAT;
    }

    public boolean a(EntityHuman entityhuman, EnumHand enumhand, @Nullable ItemStack itemstack) {
        if (itemstack != null) {
            if (this.e(itemstack) && this.getAge() == 0 && this.bx <= 0) {
                this.a(entityhuman, itemstack);
                this.c(entityhuman);
                return true;
            }

            if (this.isBaby() && this.e(itemstack)) {
                this.a(entityhuman, itemstack);
                this.setAge((int) ((float) (-this.getAge() / 20) * 0.1F), true);
                return true;
            }
        }

        return super.a(entityhuman, enumhand, itemstack);
    }

    protected void a(EntityHuman entityhuman, ItemStack itemstack) {
        if (!entityhuman.abilities.canInstantlyBuild) {
            --itemstack.count;
        }

    }

    public void c(EntityHuman entityhuman) {
        this.bx = 600;
        this.by = entityhuman;
        this.breedItem = entityhuman.inventory.getItemInHand(); // CraftBukkit
        this.world.broadcastEntityEffect(this, (byte) 18);
    }

    public EntityHuman getBreedCause() {
        return this.by;
    }

    public boolean isInLove() {
        return this.bx > 0;
    }

    public void resetLove() {
        this.bx = 0;
    }

    public boolean mate(EntityAnimal entityanimal) {
        return entityanimal != this && (entityanimal.getClass() == this.getClass() && (this.isInLove() && entityanimal.isInLove()));
    }
}
