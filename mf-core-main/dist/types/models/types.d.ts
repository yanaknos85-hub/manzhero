import * as t from 'io-ts';
export declare const ApprovalStateStatuses: t.UnionC<[t.LiteralC<"AWAITING_APPROVAL">, t.LiteralC<"APPROVED">, t.LiteralC<"DECLINED">]>;
export type ApprovalStateStatuses = t.TypeOf<typeof ApprovalStateStatuses>;
