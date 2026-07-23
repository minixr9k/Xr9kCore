package dev.minixr9k.api;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class EventBus {

    private static final EventBus INSTANCE = new EventBus();

    // Ключ - КЛАСС события (например, PlayerMoveEvent.class)
    // Значение - Список всех зарегистрированных методов плагинов, отсортированных по приоритету
    private final ConcurrentHashMap<Class<? extends Event>, List<RegisteredListener>> registeredEvents;

    private EventBus() {
        this.registeredEvents = new ConcurrentHashMap<>();
    }

    public static EventBus getInstance() {
        return EventBus.INSTANCE;
    }

    // Плагин вызывает этот метод, передавая свой класс-слушатель:
    // EventBus.getInstance().registerListener(new MyMovementListener());
    public void registerListener(EventListener listener) {
        // Берем класс переданного листенера и перебираем все его методы
        Method[] methods = listener.getClass().getDeclaredMethods();

        for (Method method : methods) {
            // Проверяем, есть ли над методом наша аннотация @CatchEvent
            if (!method.isAnnotationPresent(CatchEvent.class)) {
                continue;
            }

            // Проверяем, что у метода ровно 1 аргумент
            if (method.getParameterCount() != 1) {
                System.err.println("Ошибка: Метод " + method.getName() + " имеет @CatchEvent, но параметров не 1!");
                continue;
            }

            // Получаем тип этого единственного параметра
            Class<?> paramType = method.getParameterTypes()[0];

            // Проверяем, является ли этот параметр наследником нашего класса Event
            if (!Event.class.isAssignableFrom(paramType)) {
                System.err.println("Ошибка: Параметр метода " + method.getName() + " не является наследником Event!");
                continue;
            }

            // Приводим тип к безопасному Class<? extends Event>
            @SuppressWarnings("unchecked")
            Class<? extends Event> eventClass = (Class<? extends Event>) paramType;

            // Получаем приоритет из аннотации
            CatchEvent annotation = method.getAnnotation(CatchEvent.class);
            int priority = annotation.priority();

            // Создаем обертку для вызова
            RegisteredListener registeredListener = new RegisteredListener(listener, method, priority);

            // Кладем в карту. Если для этого события еще нет списка — создаем новый ArrayList
            registeredEvents.computeIfAbsent(eventClass, k -> new ArrayList<>()).add(registeredListener);

            // Сортируем список листенеров по приоритету (благодаря Comparable в RegisteredListener)
            Collections.sort(registeredEvents.get(eventClass));
        }
    }

    // Твое ядро (Netty хендлер) вызывает этот метод:
    // EventBus.getInstance().callEvent(new PlayerMoveEvent(player, x, y, z));
    public void callEvent(Event event) {
        // Ищем список слушателей именно для класса этого объекта
        List<RegisteredListener> listeners = registeredEvents.get(event.getClass());

        if (listeners == null || listeners.isEmpty()) {
            return; // Никто не подписался на это событие, уходим
        }

        // Проходим по всем подписавшимся плагинам и дергаем их методы
        for (RegisteredListener listener : listeners) {
            try {
                listener.execute(event);
            } catch (Exception e) {
                System.err.println("Произошла ошибка в плагине при обработке события " + event.getClass().getSimpleName());
                e.printStackTrace(); // Чтобы ошибка в плагине не крашила всё сетевое ядро
            }
        }
    }
}