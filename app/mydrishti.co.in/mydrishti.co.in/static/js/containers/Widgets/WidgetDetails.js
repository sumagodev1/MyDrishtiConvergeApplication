import React, {
    Component
} from "react";
import DragItem from "./DragItem.js";
import {
    DragDropContext
} from "react-dnd";
import MultiBackend from "react-dnd-multi-backend";
import HTML5toTouch from "react-dnd-multi-backend/lib/HTML5toTouch";
import produce from "immer";
import {
    Flipped
} from "react-flip-toolkit";

class Project extends Component {
    constructor(props) {
        super(props);
        this.state = {
            widgetDetailsData: props.widgetDetailsData,
        };
    }

    componentDidUpdate(previousProps, _previousState) {
        if (previousProps.widgetDetailsData !== this.props.widgetDetailsData) {
            this.setState((_state, props) => ({
                widgetDetailsData: props.widgetDetailsData,
            }));
        }
    }

    moveCard = (dragIndex, hoverIndex) => {
        let dragCard = this.state.widgetDetailsData[dragIndex];
        const result = produce(this.state, (draft) => {
            // Remove item from group where it was innitaly
            draft.widgetDetailsData.splice(dragIndex, 1);

            // Add to another group
            draft.widgetDetailsData.splice(hoverIndex, 0, dragCard);
        });
        this.setState(result);
        this.props.updateParameter(result);
    };

    render() {
        const {
            widgetDetailsData
        } = this.state;

        return (
            widgetDetailsData &&
            widgetDetailsData.map((item, index) => ( <
                Flipped key = {
                    item.id
                }
                flipId = {
                    item.id
                } >
                <
                DragItem item = {
                    item
                }
                key = {
                    item.id
                }
                index = {
                    index
                }
                group = {
                    index
                }
                onRemove = {
                    this.props.onRemove
                }
                editWidgetDetails = {
                    this.props.editWidgetDetails
                }
                moveCard = {
                    this.moveCard
                }
                /> <
                /Flipped>
            ))
        );
    }
}
export default DragDropContext(MultiBackend(HTML5toTouch))(Project);