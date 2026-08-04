import copy from './modules/copy'
import debounce from './modules/debounce'
import throttle from './modules/throttle'

const directivesList = {
    copy,
    debounce,
    throttle,
}

const directives = {
    install: function (app) {
        Object.keys(directivesList).forEach((key) => {
            app.directive(key, directivesList[key])
        })
    },
}

export default directives
