import React from "react";
import {
    Snackbar
} from "@material-ui/core";
import MuiAlert from "@material-ui/lab/Alert";
import {
    makeStyles
} from "@material-ui/core/styles";

const useStyles = makeStyles(() => ({
    snack: {
        marginTop: "51px",
    },
}));

const ToastMaster = (props) => {
    const classes = useStyles();
    const {
        open,
        message,
        onClose,
        severity
    } = props;

    const Alert = (props) => {
        return ( <
            MuiAlert elevation = {
                6
            }
            variant = "filled" { ...props
            }
            className = {
                classes.snack
            }
            />
        );
    };

    return ( <
        Snackbar anchorOrigin = {
            {
                vertical: "top",
                horizontal: "right",
            }
        }
        autoHideDuration = {
            3000
        }
        open = {
            open
        }
        onClose = {
            onClose
        } >
        <
        Alert onClose = {
            onClose
        }
        severity = {
            severity
        } > {
            message
        } <
        /Alert> <
        /Snackbar>
    );
};

export default ToastMaster;