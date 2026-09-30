import type { TreeDataNode as ITreeDataNode, TreeProps } from 'antd';

export default interface ITree extends TreeProps {
  search?: boolean;
  placeholder?: string;
};

export { ITreeDataNode };