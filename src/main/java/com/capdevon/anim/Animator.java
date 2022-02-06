    /**
     * Run animation
     *
     * @param anim
     */
    public void setAnimation(Animation3 anim) {
        setAnimation(anim.getName(), false);
    }
    
    public void setAnimation(String animName) {
        setAnimation(animName, false);
    }

    /**
     * 
     * @param animName
     * @param overwrite 
     */
    public void setAnimation(String animName, boolean overwrite) {

        if (overwrite || !animName.equals(currentAnim)) {
            CustomAction action = animationMap.get(animName);
            if (action != null) {
                // play animation mapped on custom action.
                action.playAnimation();
            } else {
                // play animation in a traditional way.
                animComposer.setCurrentAction(animName);
            }
            currentAnim = animName;
        }
    }
