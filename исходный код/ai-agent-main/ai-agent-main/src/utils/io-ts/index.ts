import * as t from 'io-ts';

interface UUIDBrand {
  readonly UUID: unique symbol;
}

export type UUID = t.Branded<string, UUIDBrand>;

const uuidRe = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

export const validateUUID = (s: string): s is UUID => uuidRe.test(s);

export const uuid = t.brand(t.string, validateUUID, 'UUID');
