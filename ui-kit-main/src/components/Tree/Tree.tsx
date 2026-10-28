import React, { useState, useEffect } from 'react';
import type { FC, Key, ChangeEvent } from 'react';
import { Tree as TreeAntd, Input } from 'antd';
import cn from 'classnames';

import type { ITree, ITreeDataNode} from './index';

import styles from './Tree.module.scss';

const Tree: FC<ITree> = ({ className, ...props }) => {
  const [treeData, setTreeData] = useState<ITreeDataNode[]>(props.treeData || []);
  const [dataList, setDataList] = useState<{ key: Key; title: string }[]>([]);
  const [expandedKeys, setExpandedKeys] = useState<Key[]>([]);
  const [searchValue, setSearchValue] = useState('');
  const [autoExpandParent, setAutoExpandParent] = useState(true);

  const onDrop: ITree['onDrop'] = info => {
    const dropKey = info.node.key;
    const dragKey = info.dragNode.key;
    const dropPos = info.node.pos.split('-');
    const dropPosition = info.dropPosition - Number(dropPos[dropPos.length - 1]);

    const loop = (
      data: ITreeDataNode[],
      key: Key,
      callback: (node: ITreeDataNode, i: number, data: ITreeDataNode[]) => void,
    ) => {
      for (let i = 0; i < data.length; i++) {
        if (data[i].key === key) {
          return callback(data[i], i, data);
        }
        if (data[i].children) {
          loop(data[i].children!, key, callback);
        }
      }
    };
    const data = [...treeData];

    let dragObj: ITreeDataNode;
    loop(data, dragKey, (item, index, arr) => {
      arr.splice(index, 1);
      dragObj = item;
    });

    if (!info.dropToGap) {
      loop(data, dropKey, (item) => {
        item.children = item.children || [];
        item.children.unshift(dragObj);
      });
    } else {
      let array: ITreeDataNode[] = [];
      let i: number;

      loop(data, dropKey, (_item, index, arr) => {
        array = arr;
        i = index;
      });

      if (dropPosition === -1) {
        array.splice(i!, 0, dragObj!);
      } else {
        array.splice(i! + 1, 0, dragObj!);
      }
    }

    setTreeData(data);
  };
  
  const onChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { value } = e.target;

    const getParentKey = (key: Key, tree: ITreeDataNode[]): Key => {
      let parentKey: Key;

      for (let i = 0; i < tree.length; i++) {
        const node = tree[i];

        if (node.children) {
          if (node.children.some(item => item.key === key)) {
            parentKey = node.key;
          } else if (getParentKey(key, node.children)) {
            parentKey = getParentKey(key, node.children);
          }
        }
      }

      return parentKey!;
    };

    const newExpandedKeys = dataList
      .map(item => {
        if (item.title.indexOf(value) > -1) {
          return getParentKey(item.key, props.treeData || []);
        }
        return null;
      })
      .filter((item, i, self): item is React.Key => !!(item && self.indexOf(item) === i));

    setExpandedKeys(newExpandedKeys);
    setSearchValue(value);
    setAutoExpandParent(true);
  };

  const onExpand = (newExpandedKeys: React.Key[]) => {
    setExpandedKeys(newExpandedKeys);
    setAutoExpandParent(false);
  };

  useEffect(() => {
    const generateList = (data: ITreeDataNode[]) => {
      for (let i = 0; i < data.length; i++) {
        const list = dataList;
        const node = data[i];
        const { key } = node;

        list.push({ key, title: key as string });
        setDataList(list);

        if (node.children) {
          generateList(node.children);
        }
      }
    };

    generateList(props.treeData || []);
  }, []);

  useEffect(() => {
    if (props.search) {
      const loop = (data: ITreeDataNode[]): ITreeDataNode[] => (
        data.map(item => {
          const strTitle = item.title as string;
          const index = strTitle.indexOf(searchValue);
          const beforeStr = strTitle.substring(0, index);
          const afterStr = strTitle.slice(index + searchValue.length);
          const title =
            index > -1 ? (
              <span key={item.key}>
              {beforeStr}
                <span className="site-tree-search-value">{searchValue}</span>
                {afterStr}
            </span>
            ) : (
              <span key={item.key}>{strTitle}</span>
            );
          if (item.children) {
            return { title, key: item.key, children: loop(item.children) };
          }

          return {
            title,
            key: item.key,
          };
        })
      );

      return setTreeData(loop(props.treeData || []));
    }
  }, [props.search, searchValue]);

  return (
    <>
      {props.search && (
        // TODO использовать компонент поиска когда он будет готов
        <Input.Search
          style={{ marginBottom: '10px' }}
          placeholder={props.placeholder}
          onChange={onChange}
        />
      )}
      <TreeAntd
        className={cn(styles.tree, className)}
        onDrop={props.draggable ? onDrop : undefined}
        expandedKeys={expandedKeys}
        autoExpandParent={autoExpandParent}
        onExpand={onExpand}
        {...props}
        treeData={treeData}
      />
  </>
  );
};

export default Tree;