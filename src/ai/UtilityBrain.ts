import { Agent, AgentState, PointOfInterest, SimulationWorld } from '../types';

export interface UtilityScores {
  eat: number;
  rest: number;
  work: number;
  social: number;
  entertain: number;
  wander: number;
}

export class UtilityBrain {
  /**
   * Evaluates normalized utility scores for all possible actions
   * using non-linear curves based on dynamic depleting needs.
   */
  static evaluateUtility(agent: Agent, world: SimulationWorld, allAgents: Agent[]): UtilityScores {
    const isNight = world.timeOfDayHours < 6.0 || world.timeOfDayHours > 20.5;

    // 1. Eat Utility: High urgency when hunger < 30, emergency when < 15
    const hungerDeficit = Math.max(0, 100 - agent.hunger) / 100;
    let eatScore = Math.pow(hungerDeficit, 2.2) * 1.5;
    if (agent.hunger < 25) eatScore += 0.85;

    // 2. Rest Utility: High urgency when energy < 20, boosted at night
    const energyDeficit = Math.max(0, 100 - agent.energy) / 100;
    let restScore = Math.pow(energyDeficit, 2.0) * 1.4;
    if (agent.energy < 20) restScore += 0.9;
    if (isNight) restScore += 0.5;

    // 3. Social Utility: Rises as social need drops
    const socialDeficit = Math.max(0, 100 - agent.social) / 100;
    let socialScore = Math.pow(socialDeficit, 1.6) * 1.1;
    if (agent.personality === 'Socialite') socialScore *= 1.35;

    // 4. Work & Gathering Utility: High when basic physiological needs are fulfilled
    const physiologicalReadiness = (agent.energy / 100) * (agent.hunger / 100);
    let workScore = 0.55 * Math.min(1, Math.max(0, physiologicalReadiness));
    if (agent.personality === 'Hardworker') workScore *= 1.4;
    workScore += (agent.skillLevel - 1) * 0.05;
    if (isNight || agent.energy < 30 || agent.hunger < 30) {
      workScore *= 0.2;
    }

    // 5. Entertainment Utility: Increases as boredom rises
    const boredomScore = Math.pow(agent.boredom / 100, 1.8) * 1.1;
    let entertainScore = boredomScore;

    // 6. Base wander baseline
    const wanderScore = 0.15;

    return {
      eat: eatScore,
      rest: restScore,
      work: workScore,
      social: socialScore,
      entertain: entertainScore,
      wander: wanderScore,
    };
  }

  /**
   * Decides next state based on highest utility score.
   */
  static decideNextState(agent: Agent, world: SimulationWorld, allAgents: Agent[]): Partial<Agent> {
    const utility = this.evaluateUtility(agent, world, allAgents);
    const maxScore = Math.max(
      utility.eat,
      utility.rest,
      utility.work,
      utility.social,
      utility.entertain,
      utility.wander
    );

    const getPoiTarget = (poiId: string, fallbackX: number, fallbackY: number) => {
      const poi = world.pois.find((p) => p.id === poiId);
      if (!poi) return { x: fallbackX, y: fallbackY };
      const margin = 28;
      const w = poi.bounds.width - margin * 2;
      const h = poi.bounds.height - margin * 2;
      const rx = ((agent.id * 37 + 13) % 100) / 100;
      const ry = ((agent.id * 59 + 29) % 100) / 100;
      return {
        x: poi.bounds.x + margin + rx * w,
        y: poi.bounds.y + margin + ry * h,
      };
    };

    if (maxScore === utility.eat) {
      const t = getPoiTarget('poi_food', 240, 250);
      return {
        activeState: 'SEEKING_FOOD',
        targetX: t.x,
        targetY: t.y,
        targetPoiId: 'poi_food',
        targetAgentId: undefined,
        stateDuration: 0,
        activeEmote: '🍎',
        emoteDuration: 2.5,
      };
    }

    if (maxScore === utility.rest) {
      const t = getPoiTarget('poi_rest', 760, 250);
      return {
        activeState: 'SEEKING_REST',
        targetX: t.x,
        targetY: t.y,
        targetPoiId: 'poi_rest',
        targetAgentId: undefined,
        stateDuration: 0,
        activeEmote: '😴',
        emoteDuration: 2.5,
      };
    }

    if (maxScore === utility.work) {
      const t = getPoiTarget('poi_work', 240, 1030);
      return {
        activeState: 'SEEKING_WORK',
        targetX: t.x,
        targetY: t.y,
        targetPoiId: 'poi_work',
        targetAgentId: undefined,
        stateDuration: 0,
        activeEmote: '⚒️',
        emoteDuration: 2.5,
      };
    }

    if (maxScore === utility.social) {
      const availablePartner = allAgents
        .filter((a) => a.id !== agent.id && a.activeState !== 'SLEEPING' && a.activeState !== 'EATING')
        .sort((a, b) => {
          const d1 = Math.hypot(agent.x - a.x, agent.y - a.y);
          const d2 = Math.hypot(agent.x - b.x, agent.y - b.y);
          return d1 - d2;
        })[0];

      const plazaTarget = getPoiTarget('poi_plaza', 500, 650);
      const targetPos = availablePartner && Math.hypot(agent.x - availablePartner.x, agent.y - availablePartner.y) < 260
        ? { x: availablePartner.x, y: availablePartner.y }
        : plazaTarget;

      return {
        activeState: 'SEEKING_SOCIAL',
        targetX: targetPos.x,
        targetY: targetPos.y,
        targetPoiId: 'poi_plaza',
        targetAgentId: availablePartner?.id,
        stateDuration: 0,
        activeEmote: '💬',
        emoteDuration: 2.5,
      };
    }

    if (maxScore === utility.entertain) {
      const t = getPoiTarget('poi_park', 760, 1030);
      return {
        activeState: 'ENTERTAINING',
        targetX: t.x,
        targetY: t.y,
        targetPoiId: 'poi_park',
        targetAgentId: undefined,
        stateDuration: 0,
        activeEmote: '🌸',
        emoteDuration: 2.5,
      };
    }

    // Default Wandering
    const rx = 100 + ((agent.id * 89 + Math.floor(agent.x)) % 800);
    const ry = 140 + ((agent.id * 103 + Math.floor(agent.y)) % 1050);
    return {
      activeState: 'WANDERING',
      targetX: Math.min(920, Math.max(80, rx)),
      targetY: Math.min(1240, Math.max(120, ry)),
      targetPoiId: undefined,
      targetAgentId: undefined,
      stateDuration: 0,
      activeEmote: undefined,
    };
  }
}
