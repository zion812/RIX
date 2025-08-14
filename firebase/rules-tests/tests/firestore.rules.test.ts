import { initializeTestEnvironment, RulesTestEnvironment } from '@firebase/rules-unit-testing';
import { readFileSync } from 'fs';
import { setLogLevel } from 'firebase/firestore';

let testEnv: RulesTestEnvironment;

beforeAll(async () => {
  setLogLevel('error');
  testEnv = await initializeTestEnvironment({
    projectId: 'demo-rostry',
    firestore: {
      rules: readFileSync('../../firestore.rules', 'utf8'),
    },
  });
});

afterAll(async () => {
  await testEnv.cleanup();
});

function authedContext(uid: string, claims: any = {}) {
  return testEnv.authenticatedContext(uid, { token: { tier: 'farmer', ...claims } }).firestore();
}

describe('Firestore marketplace listing rules', () => {
  test('reject create when required fields missing for traceable listing', async () => {
    const db = authedContext('user_1');
    const doc = db.collection('marketplace').doc('listing_1');
    await expect(doc.set({
      listingType: 'TRACEABLE',
      sellerId: 'user_1',
      breedType: 'RIR',
      // missing age, parentIds, healthRecords, verificationStatus, lineageVerified, ownerHistory
    })).rejects.toThrow();
  });

  test('allow create for valid traceable listing with age > 12 requires extra fields', async () => {
    const db = authedContext('user_2');
    const doc = db.collection('marketplace').doc('listing_2');
    await expect(doc.set({
      listingType: 'TRACEABLE',
      sellerId: 'user_2',
      breedType: 'RIR',
      age: 13,
      parentIds: ['p1', 'p2'],
      healthRecords: [],
      verificationStatus: 'verified',
      lineageVerified: true,
      ownerHistory: [],
      breedingHistory: [],
      performanceMetrics: {},
    })).resolves.toBeUndefined();
  });

  test('allow create for valid non-traceable listing', async () => {
    const db = authedContext('user_3');
    const doc = db.collection('marketplace').doc('listing_3');
    await expect(doc.set({
      listingType: 'NON_TRACEABLE',
      sellerId: 'user_3',
      breedType: 'RIR',
      approximateAge: 8,
      currentHealth: 'GOOD',
      sellerVerification: 'basic',
    })).resolves.toBeUndefined();
  });
});
