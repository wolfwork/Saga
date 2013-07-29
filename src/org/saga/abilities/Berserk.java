package org.saga.abilities;

import org.saga.SagaLogger;
import org.saga.attributes.DamageType;
import org.saga.exceptions.InvalidAbilityException;
import org.saga.listeners.events.SagaDamageEvent;

public class Berserk extends Ability{

	/**
	 * Hits required value key.
	 */
	transient private static String HITS_REQUIRED = "hits required";

	/**
	 * Damage multiplier value key.
	 */
	transient private static String DAMAGE_MULTIPLIER = "damage multiplier";
	
	
	/**
	 * Hits made.
	 */
	private Integer hit;
	
	
	
	/**
	 * Initialises using definition.
	 * 
	 * @param definition ability definition
	 */
	public Berserk(AbilityDefinition definition) {
		
        super(definition);

		hit = 0;
		
	}
	
	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.abilities.Ability#complete()
	 */
	@Override
	public boolean complete() throws InvalidAbilityException {
		
		super.complete();
	
		if (hit == null) {
			SagaLogger.nullField(this, "hit");
			hit = 0;
		}
		
		return true;
		
	}

	
	

	
	// Usage:
	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.abilities.Ability#handleAttackPreTrigger(org.saga.listeners.events.SagaEntityDamageEvent)
	 */
	@Override
	public boolean handleAttackPreTrigger(SagaDamageEvent event) {
		return handlePreTrigger();
	}
	
	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.abilities.Ability#useSilentPreTrigger()
	 */
	@Override
	public boolean useSilentPreTrigger() {
		return true;
	}
	
	@Override
	public boolean handleDefendPreTrigger(SagaDamageEvent event) {
		return hit > 0;
	}
	
	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.abilities.Ability#triggerAttack(org.saga.listeners.events.SagaEntityDamageEvent)
	 */
	@Override
	public boolean triggerAttack(SagaDamageEvent event) {
		
		if(event.isCancelled()) return false;
		
		// Increase hits:
		hit++;
		
		// Only melee:
		if(event.type != DamageType.MELEE ) return false;
		
		// Enough hits:
		if(hit < getDefinition().getFunction(HITS_REQUIRED).value(getScore())) return false;
		
		// Increase damage:
		double mult = getDefinition().getFunction(DAMAGE_MULTIPLIER).value(getScore());
		event.multiplyDamage(mult);
		
		// Reset hits:
		hit = 0;
		
		return true;
		
	}
	
	/* 
	 * Resets hits.
	 * 
	 * @see org.saga.abilities.Ability#triggerDefend(org.saga.listeners.events.SagaEntityDamageEvent)
	 */
	@Override
	public boolean triggerDefend(SagaDamageEvent event) {
		
		hit = 0;
		return false;
	
	}
	
	
}
