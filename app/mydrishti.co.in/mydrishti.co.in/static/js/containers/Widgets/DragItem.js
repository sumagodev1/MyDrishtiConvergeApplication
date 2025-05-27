import React, {
    useImperativeHandle,
    useRef
} from "react";
import {
    DragSource,
    DropTarget
} from "react-dnd";
import Charts from "./Charts";

const Types = {
    WIDGET: "widget",
};

const cardSource = {
    beginDrag(props) {
        let item = { ...props.item
        };
        item.index = props.index;
        item.group = props.group;
        return item;
    },
    endDrag(props, monitor) {
        //console.log(props);
        //console.log(monitor.getItem());
        //console.log(monitor.getDropResult());
    },
};

const widgetTarget = {
    hover(props, monitor, component) {
        if (!component) {
            return null;
        }
        const node = component.getNode();
        if (!node) {
            return null;
        }

        const dragIndex = monitor.getItem().index;
        const hoverIndex = props.index;
        const dragGroup = monitor.getItem().group;
        const targetGroup = props.group;

        if (dragIndex === hoverIndex && dragGroup === targetGroup) {
            return;
        }

        const hoverBoundingRect = node.getBoundingClientRect();

        if (
            Math.abs(monitor.getClientOffset().x - hoverBoundingRect.left) >
            hoverBoundingRect.width / 1.8
        )
            return;

        props.moveCard(dragIndex, hoverIndex, dragGroup, targetGroup);
        monitor.getItem().index = hoverIndex;
        monitor.getItem().group = targetGroup;
    },
};

function collect(connect, monitor) {
    return {
        connectDragSource: connect.dragSource(),
        isDragging: monitor.isDragging(),
        getItem: monitor.getItem(),
    };
}

const DragItem = React.forwardRef(
    ({
            item,
            getItem,
            connectDragSource,
            connectDropTarget,
            onRemove,
            editWidgetDetails,
        },
        ref
    ) => {
        const elementRef = useRef(null);
        connectDragSource(elementRef);
        connectDropTarget(elementRef);
        let draggedId = null;
        if (getItem !== null) {
            draggedId = getItem.id;
        }

        const dragClass = draggedId === item.id ? "draggedItem" : "";

        useImperativeHandle(ref, () => ({
            getNode: () => elementRef.current,
        }));

        return ( <
            div ref = {
                elementRef
            }
            className = {
                dragClass
            } >
            <
            Charts widgetData = {
                item
            }
            onRemove = {
                onRemove
            }
            editWidgetDetails = {
                editWidgetDetails
            }
            /> <
            /div>
        );
    }
);

export default DropTarget(Types.WIDGET, widgetTarget, (connect, monitor) => ({
    connectDropTarget: connect.dropTarget(),
    isOver: monitor.isOver(),
    isOverCurrent: monitor.isOver({
        shallow: true
    }),
    canDrop: monitor.canDrop(),
    itemType: monitor.getItemType(),
}))(DragSource(Types.WIDGET, cardSource, collect)(DragItem));