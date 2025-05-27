import {
    UPDATE_FREQUENCY_TIME
} from "./types"

const initialState = {
    frequencyUpdateSeconds: 300,
    refreshDuration: 5
}

// eslint-disable-next-line import/no-anonymous-default-export
export default (state = initialState, {
    type,
    payload = initialState.frequencyUpdateSeconds
}) => {
    switch (type) {
        case UPDATE_FREQUENCY_TIME:
            {
                return { ...state,
                    frequencyUpdateSeconds: payload
                }
            }
        default:
            return state
    }
}