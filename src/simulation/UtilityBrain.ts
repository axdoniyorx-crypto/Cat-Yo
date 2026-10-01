import { Agent, AgentState, PointOfInterest, SimulationWorld, STATE_ICONS } from './types';

export class UtilityBrain {
  static evaluateUtility(agent: Agent, world: SimulationWorld, allAgents: Agent[]) {
    // 1. Food Utility (Depletes 100 -> 0)
    const hungerDeficit = Math.max(0, Math.min(100, 100 - agent.hunger)) / 100;
    let eatScore = Math.pow(hungerDeficit, 2.2) * 1.5;
    if (agent.hunger < 25) eatScore += 0.8;
    if (agent.personality === 'Epicurean Explorer') eatScore *= 1.25;

    // 2. Rest Utility (Depletes 100 -> 0)
    const energyDeficit = Math.max(0, Math.min(100, 100 - agent.energy)) / 100;
    let restScore = Math.pow(energyDeficit, 2.0) * 1.4;
    if (agent.energy < 20) restScore += 0.9;
    const isNight = world.timeOfDayHours < 6 || world.timeOfDayHours > 20;
    if (isNight) restScore += 0.45;

    // 3. Social Utility
    const socialDeficit = Math.max(0, Math.min(100, 100 - agent.social)) / 100;
    let socialScore = Math.pow(socialDeficit, 1.6) * 1.1;
    if (agent.personality === 'Outgoing Conversationalist') socialScore *= 1.4;

    // 4. Work Utility
    const readiness = (agent.energy / 100) * (agent.hunger / 100);
    let workScore = 0.55 * Math.max(0, Math.min(1, readiness));
    if (agent.personality === 'Industrious Builder') workScore *= 1.4;
    workScore += (agent.skillLevel - 1) * 0.05;
    if (isNight || agent.energy < 30 || agent.hunger < 30) workScore *= 0.2;

    // 5. Entertainment Utility
    let entertainScore = Math.pow(agent.boredom / 100, 1.8) * 1.1;
    if (agent.personality === 'Curious Thinker') entertainScore *= 1.2;

    // 6. Wander baseline
    const wanderScore = 0.15;

    return { eatScore, restScore, workScore, socialScore, entertainScore, wanderScore };
  }

  static decideNextState(agent: Agent, world: SimulationWorld, allAgents: Agent[]): Partial<Agent> {
    const scores = this.evaluateUtility(agent, world, allAgents);
    const maxVal = Math.max(
      scores.eatScore,
      scores.restScore,
      scores.workScore,
      scores.socialScore,
      scores.entertainScore,
      scores.wanderScore
    );

    if (maxVal === scores.eatScore) {
      const poi = world.pois.find(p => p.type === 'FOOD_SOURCE');
      return {
        activeState: 'Seeking Food',
        targetPosition: poi ? this.getPoiInteriorTarget(poi, agent.id) : { x: 200, y: 250 },
        targetPoiId: poi?.id || null,
        targetAgentId: null,
        stateDuration: 0,
        activeEmote: '🍎',
        emoteDuration: 2.5
      };
    }

    if (maxVal === scores.restScore) {
      const poi = world.pois.find(p => p.type === 'REST_AREA');
      return {
        activeState: 'Seeking Rest',
        targetPosition: poi ? this.getPoiInteriorTarget(poi, agent.id) : { x: 750, y: 250 },
        targetPoiId: poi?.id || null,
        targetAgentId: null,
        stateDuration: 0,
        activeEmote: '😴',
        emoteDuration: 2.5
      };
    }

    if (maxVal === scores.workScore) {
      const poi = world.pois.find(p => p.type === 'WORK_AREA');
      return {
        activeState: 'Seeking Work',
        targetPosition: poi ? this.getPoiInteriorTarget(poi, agent.id) : { x: 200, y: 1000 },
        targetPoiId: poi?.id || null,
        targetAgentId: null,
        stateDuration: 0,
        activeEmote: '⚒️',
        emoteDuration: 2.5
      };
    }

    if (maxVal === scores.socialScore) {
      const available = allAgents.filter(
        a => a.id !== agent.id && a.activeState !== 'Sleeping' && a.activeState !== 'Eating'
      );
      const partner = available.length > 0
        ? available.reduce((closest, curr) => {
            const d1 = this.dist(agent.position, closest.position);
            const d2 = this.dist(agent.position, curr.position);
            return d2 < d1 ? curr : closest;
          })
        : null;

      const plaza = world.pois.find(p => p.type === 'TOWN_PLAZA');
      const target = partner && this.dist(agent.position, partner.position) < 250
        ? { ...partner.position }
        : (plaza ? this.getPoiInteriorTarget(plaza, agent.id) : { x: 500, y: 650 });

      return {
        activeState: 'Seeking Social',
        targetPosition: target,
        targetPoiId: plaza?.id || null,
        targetAgentId: partner ? partner.id : null,
        stateDuration: 0,
        activeEmote: '💬',
        emoteDuration: 2.5
      };
    }

    if (maxVal === scores.entertainScore) {
      const park = world.pois.find(p => p.type === 'RECREATION_PARK');
      return {
        activeState: 'Entertaining',
        targetPosition: park ? this.getPoiInteriorTarget(park, agent.id) : { x: 750, y: 1000 },
        targetPoiId: park?.id || null,
        targetAgentId: null,
        stateDuration: 0,
        activeEmote: '🌸',
        emoteDuration: 2.5
      };
    }

    // Wander
    const rx = 100 + ((agent.id * 89 + Math.floor(agent.position.x)) % 800);
    const ry = 150 + ((agent.id * 103 + Math.floor(agent.position.y)) % 1050);
    return {
      activeState: 'Wandering',
      targetPosition: { x: Math.max(80, Math.min(920, rx)), y: Math.max(120, Math.min(1230, ry)) },
      targetPoiId: null,
      targetAgentId: null,
      stateDuration: 0,
      activeEmote: null
    };
  }

  static getPoiInteriorTarget(poi: PointOfInterest, seed: number) {
    const margin = 26;
    const w = Math.max(10, poi.bounds.right - poi.bounds.left - margin * 2);
    const h = Math.max(10, poi.bounds.bottom - poi.bounds.top - margin * 2);
    const rx = ((seed * 37 + 13) % 100) / 100;
    const ry = ((seed * 59 + 29) % 100) / 100;
    return {
      x: poi.bounds.left + margin + rx * w,
      y: poi.bounds.top + margin + ry * h
    };
  }

  static dist(a: { x: number; y: number }, b: { x: number; y: number }) {
    const dx = a.x - b.x;
    const dy = a.y - b.y;
    return Math.sqrt(dx * dx + dy * dy);
  }
}
