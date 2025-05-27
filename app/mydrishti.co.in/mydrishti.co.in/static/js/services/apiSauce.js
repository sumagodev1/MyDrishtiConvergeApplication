import {
    create
} from "apisauce";

const api = create({});

class ApiSauce {
    async post(url, payload, headers) {
        const response = await api.post(url, payload, headers);
        return new Promise((resolve, reject) => {
            this.handlePromise(resolve, reject, response);
        });
    }

    async postWithToken(url, payload, token) {
        const Header = {
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`,
            },
        };

        const response = await api.post(url, payload, Header);

        return new Promise((resolve, reject) => {
            this.handlePromise(resolve, reject, response);
        });
    }

    async get(url, payload) {
        api.setHeaders({
            "Content-Type": "application/json",
        });

        const response = await api.get(url, payload);

        return new Promise((resolve, reject) => {
            this.handlePromise(resolve, reject, response);
        });
    }

    async postWithCustomHeader(url, header) {
        const response = await api.post(url, header);

        return new Promise((resolve, reject) => {
            this.handlePromise(resolve, reject, response);
        });
    }

    async getWithCustomHeader(url, header) {
        const response = await api.get(url, {}, header);
        if (response.status === 401) {
            window.location.replace("http://localhost:3000/");
            localStorage.removeItem('accessToken')
            localStorage.removeItem('persist:root')
            localStorage.removeItem('currentUser')
            localStorage.removeItem('currentModule')
            localStorage.removeItem('widgetSettings')
            localStorage.removeItem('parametersList')
            localStorage.removeItem('deviceList')
        }

        return new Promise((resolve, reject) => {
            this.handlePromise(resolve, reject, response);
        });
    }

    handlePromise = (resolve, reject, response) => {
        if (response.ok && response.data && response.originalError === null) {
            resolve(response.data);
        } else if (response.data && !response.ok) {
            resolve({
                data: response.data,
                status: response.status
            });
        } else {
            if (
                response.status === 404 &&
                !response.ok &&
                response.originalError !== null
            ) {
                reject(response.problem);
            } else if (response.status === 401) {
                this.props.history.push("/login");

                alert("hello");
            }
        }
    };
}

export default new ApiSauce();