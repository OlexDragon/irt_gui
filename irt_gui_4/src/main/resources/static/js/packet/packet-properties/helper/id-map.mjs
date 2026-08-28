export function createIdMap(definition, options = {}) {
    const map = Array.isArray(definition)
        ? arrayToValue(definition)
        : objectToValue(definition);

    const reverse = Object.freeze(
        Object.fromEntries(
            Object.entries(map).map(([name, code]) => [
                code,
                name
            ])
        )
    );

    const info = Object.freeze(
        Object.fromEntries(
            Object.entries(map).map(([name, code]) => [
                name,
                Object.freeze({ code, name })
            ])
        )
    );

    const id = value => {
        if (typeof value === 'number')
            return value;

        return map[value];
    };

    const name = value => {
        if (typeof value === 'string')
            return map[value] !== undefined ? value : undefined;

        return reverse[value];
    };

    const toString = value => {
        const code = id(value);
        const n = name(value);

        return options.prefix
            ? `${options.prefix}: ${n} (${code})`
            : `${n} (${code})`;
    };

    return Object.freeze({
        map,
        info,
        id,
        name,
        toString
    });
}

function objectToValue(object) {
    return Object.freeze({ ...object });
}

function arrayToValue(array) {
    return Object.freeze(
        array.reduce((obj, value, index) => {
            obj[value] = index;
            return obj;
        }, {})
    );
}