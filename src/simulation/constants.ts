import { PointOfInterest } from './types';

export const PRESET_NAMES = [
  'Reno',     // Required Agent 1
  'Ather',    // Required Agent 2
  'Liora',    // Required Agent 3
  'Kael',
  'Vesper',
  'Dax',
  'Maya',
  'Zephyr',
  'Lyra',
  'Orion',
  'Silas',
  'Nova',
  'Finn',
  'Cassian',
  'Rowan',
  'Tessa',
  'Bram',
  'Mira',
  'Jace',
  'Elora'
];

export const AGENT_PALETTES = [
  '#38EF7D', // Emerald neon
  '#00C9FF', // Cyan electric
  '#FF416C', // Vibrant rose
  '#FFA07A', // Light salmon
  '#9D50BB', // Royal purple
  '#FFD200', // Sunflower yellow
  '#4FACFE', // Azure breeze
  '#FF6A00', // Tangerine
  '#20BF6B', // Jade
  '#EB3B5A', // Crimson
  '#0FB9B1', // Turquoise
  '#45AAF2', // Dodger blue
  '#FA8231', // Orange
  '#A55EEA', // Lavender purple
  '#26DE81', // Mint
  '#FC5C65', // Coral red
  '#2BCBBA', // Teal
  '#FD9644', // Amber
  '#8854D0', // Deep violet
  '#3867D6'  // Cobalt
];

export const DEFAULT_POIS: PointOfInterest[] = [
  {
    id: 'poi_food',
    name: 'Berry Orchard & Farm',
    type: 'FOOD_SOURCE',
    bounds: { left: 40, top: 110, right: 440, bottom: 400 },
    color: '#1E3A2B',
    accentColor: '#2ECC71',
    emoji: '🍏',
    description: 'Nutritious orchard and hydroponic berry garden. Satisfies hunger.',
    resourceType: 'Berries'
  },
  {
    id: 'poi_rest',
    name: 'Cozy Cabins & Dorms',
    type: 'REST_AREA',
    bounds: { left: 560, top: 110, right: 960, bottom: 400 },
    color: '#1A2744',
    accentColor: '#4A90E2',
    emoji: '🛌',
    description: 'Warm sleeping pods and plush beds. Restores stamina and energy.'
  },
  {
    id: 'poi_plaza',
    name: 'Central Plaza & Fountain',
    type: 'TOWN_PLAZA',
    bounds: { left: 310, top: 500, right: 690, bottom: 790 },
    color: '#32274A',
    accentColor: '#9B59B6',
    emoji: '⛲',
    description: 'Vibrant gathering plaza with marble fountain. Social hub of the city.'
  },
  {
    id: 'poi_work',
    name: 'Workshop & Quarry',
    type: 'WORK_AREA',
    bounds: { left: 40, top: 890, right: 440, bottom: 1180 },
    color: '#3D271D',
    accentColor: '#E67E22',
    emoji: '⚒️',
    description: 'Timber yard and artisan crafting workshop. Trains gathering skills.',
    resourceType: 'Timber & Ore'
  },
  {
    id: 'poi_park',
    name: 'Zen Garden & Lake',
    type: 'RECREATION_PARK',
    bounds: { left: 560, top: 890, right: 960, bottom: 1180 },
    color: '#19323A',
    accentColor: '#1ABC9C',
    emoji: '🌸',
    description: 'Peaceful walking paths, cherry blossom trees, and koi lake. Alleviates boredom.'
  }
];
