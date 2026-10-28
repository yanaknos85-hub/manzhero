/**
 * Метод для склонения чисел
 *
 * @param number число для которого нужно получить числительное
 * @param words склонения. Пример: ['год', 'года', 'лет']
 */
// eslint-disable-next-line @stylistic/max-len
export var declOfNum = function (number, words) { return words[number % 100 > 4 && number % 100 < 20 ? 2 : [2, 0, 1, 1, 1, 2][number % 10 < 5 ? Math.abs(number) % 10 : 5]]; };
export var declOfNumForSymbols = function (number) { return declOfNum(number, ['символ', 'символа', 'символов']); };
